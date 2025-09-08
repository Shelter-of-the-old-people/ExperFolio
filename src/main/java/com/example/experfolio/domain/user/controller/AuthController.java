package com.example.experfolio.domain.user.controller;

import com.example.experfolio.domain.user.dto.*;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.service.AuthService;
import com.example.experfolio.domain.user.service.UserService;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.security.jwt.JwtTokenInfo;
import com.example.experfolio.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 * 회원가입, 로그인, 로그아웃, 토큰 관리 등의 인증 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관리 API")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> signup(
            @Valid @RequestBody SignUpRequestDto signUpRequest) {
        
        log.info("회원가입 요청: email={}, role={}", signUpRequest.getEmail(), signUpRequest.getRole());
        
        // 비밀번호 확인 검증
        if (!signUpRequest.isPasswordMatching()) {
            throw new BadRequestException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 회원가입 처리
        User user = authService.register(
            signUpRequest.getEmail(),
            signUpRequest.getPassword(),
            signUpRequest.getRole()
        );
        
        // 사용자 추가 정보 업데이트
        user = userService.updateUserInfo(user.getId(), signUpRequest.getName(), signUpRequest.getPhoneNumber());
        
        // 응답 DTO 생성
        UserInfoResponseDto responseDto = convertToUserInfoResponse(user);
        
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.", responseDto));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequest) {
        
        log.info("로그인 요청: email={}", loginRequest.getEmail());
        
        // 로그인 처리
        JwtTokenInfo tokenInfo = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        
        // 사용자 정보 조회
        User user = userService.findActiveByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("사용자 정보를 찾을 수 없습니다."));
        
        // 응답 DTO 생성
        LoginResponseDto responseDto = LoginResponseDto.builder()
                .accessToken(tokenInfo.getAccessToken())
                .refreshToken(tokenInfo.getRefreshToken())
                .tokenType("Bearer")
                .accessTokenExpiresAt(tokenInfo.getAccessTokenExpiresAt())
                .refreshTokenExpiresAt(tokenInfo.getRefreshTokenExpiresAt())
                .userInfo(convertToUserInfo(user))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", responseDto));
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    public ResponseEntity<ApiResponse<JwtTokenInfo>> refreshToken(
            @Parameter(description = "Refresh Token", required = true)
            @RequestHeader("Refresh-Token") String refreshToken) {
        
        log.info("토큰 갱신 요청");
        
        JwtTokenInfo tokenInfo = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다.", tokenInfo));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("로그아웃 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        
        // 토큰에서 사용자 ID 추출
        java.util.UUID userId = authService.getUserIdFromToken(accessToken);
        
        // 로그아웃 처리
        authService.logout(userId);
        
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    /**
     * 이메일 인증
     */
    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰을 사용하여 이메일을 인증합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "이메일 인증 토큰", required = true)
            @RequestParam("token") String token) {
        
        log.info("이메일 인증 요청: token={}", token);
        
        authService.verifyEmail(token);
        
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", null));
    }

    /**
     * 이메일 인증 재전송
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "이메일 인증 재전송", description = "이메일 인증 토큰을 재전송합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 재전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(
            @Parameter(description = "이메일 주소", required = true)
            @RequestParam("email") String email) {
        
        log.info("이메일 인증 재전송 요청: email={}", email);
        
        authService.resendEmailVerification(email);
        
        return ResponseEntity.ok(ApiResponse.success("인증 이메일이 재전송되었습니다.", null));
    }

    /**
     * 비밀번호 재설정 요청
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 링크를 이메일로 전송합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재설정 링크 전송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "이메일 주소", required = true)
            @RequestParam("email") String email) {
        
        log.info("비밀번호 재설정 요청: email={}", email);
        
        authService.requestPasswordReset(email);
        
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 링크가 이메일로 전송되었습니다.", null));
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "재설정 토큰을 사용하여 새로운 비밀번호로 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "재설정 토큰", required = true)
            @RequestParam("token") String token,
            @Parameter(description = "새로운 비밀번호", required = true)
            @RequestParam("password") String newPassword) {
        
        log.info("비밀번호 재설정: token={}", token);
        
        authService.resetPassword(token, newPassword);
        
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다.", null));
    }

    /**
     * 현재 사용자 정보 조회 (토큰 기반)
     */
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "Access Token을 사용하여 현재 로그인된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getCurrentUser(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("현재 사용자 정보 조회 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        
        User user = authService.getCurrentUser(accessToken);
        UserInfoResponseDto responseDto = convertToUserInfoResponse(user);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    // ===== 유틸리티 메소드 =====

    /**
     * Authorization 헤더에서 토큰 추출
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadRequestException("유효하지 않은 Authorization 헤더입니다.");
        }
        return authorizationHeader.substring(7);
    }

    /**
     * User 엔티티를 UserInfoResponseDto로 변환
     */
    private UserInfoResponseDto convertToUserInfoResponse(User user) {
        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .profileCompletionRate(calculateProfileCompletionRate(user))
                .build();
    }

    /**
     * User 엔티티를 LoginResponseDto.UserInfoDto로 변환
     */
    private LoginResponseDto.UserInfoDto convertToUserInfo(User user) {
        return LoginResponseDto.UserInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * 프로필 완성도 계산 (간단한 구현)
     */
    private Integer calculateProfileCompletionRate(User user) {
        int completedFields = 0;
        int totalFields = 5; // 이메일, 이름, 전화번호, 이메일인증, 역할별 프로필
        
        if (user.getEmail() != null) completedFields++;
        if (user.getName() != null) completedFields++;
        if (user.getPhoneNumber() != null) completedFields++;
        if (user.isEmailVerified()) completedFields++;
        
        // 역할별 프로필 존재 여부 체크 (추후 구현)
        if (user.getRole() == UserRole.JOB_SEEKER) {
            // JobSeekerProfile 존재 여부 확인
            // 현재는 기본값 처리
            completedFields++; 
        } else if (user.getRole() == UserRole.RECRUITER) {
            // RecruiterProfile 존재 여부 확인  
            // 현재는 기본값 처리
            completedFields++;
        }
        
        return (completedFields * 100) / totalFields;
    }
}