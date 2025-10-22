package com.example.experfolio.domain.user.service;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.UnauthorizedException;
import com.example.experfolio.global.security.jwt.JwtTokenInfo;
import com.example.experfolio.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public User register(String email, String password, UserRole role) {
        log.info("회원가입 요청: email={}, role={}", email, role);
        
        // 이메일 중복 확인
        if (!userService.isEmailAvailable(email)) {
            throw new BadRequestException("이미 사용 중인 이메일입니다: " + email);
        }
        
        // 사용자 생성
        User user = userService.createUser(email, password, role);

        log.info("회원가입 완료: userId={}, email={}, role={}", user.getId(), email, role);
        
        return user;
    }

    // 로그인
    public JwtTokenInfo login(String email, String password) {
        log.info("로그인 요청: email={}", email);
        
        // 사용자 존재 확인
        User user = userService.findActiveByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다"));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 계정 삭제 여부 확인
        if (user.isDeleted()) {
            throw new UnauthorizedException("삭제된 계정입니다. 관리자에게 문의하세요");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getId());
        
        // 마지막 로그인 시간 업데이트
        userService.updateLastLoginTime(user.getId());
        
        log.info("로그인 성공: userId={}, email={}, role={}", user.getId(), email, user.getRole());
        
        return jwtTokenProvider.createTokenInfo(accessToken, refreshToken);
    }

    // 토큰 갱신
    public JwtTokenInfo refreshToken(String refreshToken) {
        log.info("토큰 갱신 요청");
        
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다");
        }
        
        // 토큰에서 사용자 정보 추출
        String email = jwtTokenProvider.getUserEmail(refreshToken);
        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        
        // 사용자 존재 및 활성 상태 확인
        User user = userService.findActiveByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));

        if (user.isDeleted()) {
            throw new UnauthorizedException("삭제된 계정입니다");
        }

        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole(), user.getId());
        
        log.info("토큰 갱신 완료: userId={}", userId);
        
        return jwtTokenProvider.createTokenInfo(newAccessToken, refreshToken);
    }

    // 로그아웃 (클라이언트에서 토큰 삭제, 서버에서는 별도 처리 불필요)
    public void logout(UUID userId) {
        log.info("로그아웃: userId={}", userId);
        
        // 필요한 경우 로그아웃 시간 기록 등 추가 처리
        // 현재는 클라이언트에서 토큰을 삭제하는 것으로 충분
        
        log.info("로그아웃 완료: userId={}", userId);
    }

    // 비밀번호 재설정 요청
    public void requestPasswordReset(String email) {
        log.info("비밀번호 재설정 요청: email={}", email);
        
        userService.requestPasswordReset(email);
        
        log.info("비밀번호 재설정 요청 완료: email={}", email);
    }

    // 비밀번호 재설정
    public void resetPassword(String token, String newPassword) {
        log.info("비밀번호 재설정: token={}", token);
        
        userService.resetPassword(token, newPassword);
        
        log.info("비밀번호 재설정 완료: token={}", token);
    }

    // 비밀번호 변경 (로그인된 사용자)
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 요청: userId={}", userId);
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다: " + userId));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("현재 비밀번호가 올바르지 않습니다");
        }
        
        // 새 비밀번호로 업데이트
        userService.updatePassword(userId, newPassword);
        
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    // 사용자 정보 조회 (JWT에서 추출)
    public User getCurrentUser(String accessToken) {
        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 Access Token입니다");
        }
        
        String email = jwtTokenProvider.getUserEmail(accessToken);
        
        return userService.findActiveByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));
    }

    // 토큰 유효성 검증
    public boolean isTokenValid(String accessToken) {
        return jwtTokenProvider.validateAccessToken(accessToken);
    }

    // 사용자 역할 확인
    public boolean hasRole(String accessToken, UserRole role) {
        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            return false;
        }
        
        UserRole userRole = jwtTokenProvider.getUserRole(accessToken);
        return userRole == role;
    }

    // 사용자 ID 추출
    public UUID getUserIdFromToken(String accessToken) {
        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 Access Token입니다");
        }
        
        return jwtTokenProvider.getUserId(accessToken);
    }

    // 토큰에서 사용자 이메일 추출
    public String getUserEmailFromToken(String accessToken) {
        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 Access Token입니다");
        }
        
        return jwtTokenProvider.getUserEmail(accessToken);
    }
}