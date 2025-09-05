package com.example.experfolio.domain.user.service;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.entity.UserStatus;
import com.example.experfolio.domain.user.repository.UserRepository;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 토큰 만료 시간 설정 (24시간)
    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60; // 24시간 (초 단위)
    private static final long PASSWORD_RESET_TOKEN_EXPIRY = 2 * 60 * 60; // 2시간 (초 단위)

    @Override
    public User createUser(String email, String password, UserRole role) {
        log.info("새 사용자 생성 요청: email={}, role={}", email, role);
        
        // 이메일 중복 체크
        if (existsByEmail(email)) {
            throw new BadRequestException("이미 사용 중인 이메일입니다: " + email);
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .build();
        
        // 이메일 인증 토큰 생성
        String verificationToken = generateUniqueToken();
        user.setEmailVerificationToken(verificationToken);
        
        User savedUser = userRepository.save(user);
        log.info("새 사용자 생성 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findActiveByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmailVerificationToken(String token) {
        return userRepository.findByEmailVerificationToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByValidPasswordResetToken(String token) {
        return userRepository.findByValidPasswordResetToken(token, LocalDateTime.now());
    }

    @Override
    public void activateUser(UUID userId) {
        log.info("사용자 활성화: userId={}", userId);
        updateUserStatus(userId, UserStatus.ACTIVE);
    }

    @Override
    public void deactivateUser(UUID userId) {
        log.info("사용자 비활성화: userId={}", userId);
        updateUserStatus(userId, UserStatus.INACTIVE);
    }

    @Override
    public void suspendUser(UUID userId) {
        log.info("사용자 일시정지: userId={}", userId);
        updateUserStatus(userId, UserStatus.SUSPENDED);
    }

    @Override
    public void updateUserStatus(UUID userId, UserStatus status) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (user.isDeleted()) {
            throw new BadRequestException("삭제된 사용자의 상태를 변경할 수 없습니다");
        }
        
        userRepository.updateUserStatus(userId, status);
        log.info("사용자 상태 업데이트 완료: userId={}, status={}", userId, status);
    }

    @Override
    public void verifyEmail(String token) {
        log.info("이메일 인증 요청: token={}", token);
        
        User user = findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("유효하지 않은 인증 토큰입니다"));
        
        if (user.isEmailVerified()) {
            throw new BadRequestException("이미 인증된 이메일입니다");
        }
        
        user.verifyEmail();
        user.activate(); // 이메일 인증 시 자동 활성화
        userRepository.save(user);
        
        log.info("이메일 인증 완료: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailVerificationToken(UUID userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (user.isEmailVerified()) {
            throw new BadRequestException("이미 인증된 이메일입니다");
        }
        
        String token = generateUniqueToken();
        user.setEmailVerificationToken(token);
        userRepository.save(user);
        
        // TODO: 실제 이메일 발송 로직 구현 필요
        log.info("이메일 인증 토큰 발송: userId={}, email={}, token={}", 
                userId, user.getEmail(), token);
    }

    @Override
    public String generateEmailVerificationToken(UUID userId) {
        String token = generateUniqueToken();
        
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setEmailVerificationToken(token);
        userRepository.save(user);
        
        return token;
    }

    @Override
    public void updatePassword(UUID userId, String newPassword) {
        log.info("비밀번호 업데이트: userId={}", userId);
        
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);
        
        log.info("비밀번호 업데이트 완료: userId={}", userId);
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("비밀번호 재설정 요청: email={}", email);
        
        User user = findActiveByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        String token = generateUniqueToken();
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(PASSWORD_RESET_TOKEN_EXPIRY);
        user.setPasswordResetToken(token, expiry);
        userRepository.save(user);
        
        // TODO: 실제 이메일 발송 로직 구현 필요
        log.info("비밀번호 재설정 토큰 발송: email={}, token={}, expiry={}", 
                email, token, expiry);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("비밀번호 재설정: token={}", token);
        
        User user = findByValidPasswordResetToken(token)
                .orElseThrow(() -> new BadRequestException("유효하지 않거나 만료된 재설정 토큰입니다"));
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);
        
        log.info("비밀번호 재설정 완료: userId={}", user.getId());
    }

    @Override
    public String generatePasswordResetToken(UUID userId) {
        String token = generateUniqueToken();
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(PASSWORD_RESET_TOKEN_EXPIRY);
        
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setPasswordResetToken(token, expiry);
        userRepository.save(user);
        
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordResetTokenValid(String token) {
        return findByValidPasswordResetToken(token).isPresent();
    }

    @Override
    public void updateLastLoginTime(UUID userId) {
        updateLastLoginTime(userId, LocalDateTime.now());
    }

    @Override
    public void updateLastLoginTime(UUID userId, LocalDateTime loginTime) {
        userRepository.updateLastLoginAt(userId, loginTime);
        log.debug("로그인 시간 업데이트: userId={}, loginTime={}", userId, loginTime);
    }

    @Override
    public User updateUser(User user) {
        User existingUser = findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + user.getId()));
        
        if (existingUser.isDeleted()) {
            throw new BadRequestException("삭제된 사용자는 수정할 수 없습니다");
        }
        
        User savedUser = userRepository.save(user);
        log.info("사용자 정보 업데이트 완료: userId={}", user.getId());
        
        return savedUser;
    }

    @Override
    public void softDeleteUser(UUID userId) {
        log.info("사용자 소프트 삭제: userId={}", userId);
        
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (user.isDeleted()) {
            throw new BadRequestException("이미 삭제된 사용자입니다");
        }
        
        user.softDelete();
        userRepository.save(user);
        
        log.info("사용자 소프트 삭제 완료: userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRoleAndStatus(role, UserStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActiveUsersOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUnverifiedUsers(LocalDateTime cutoffDate) {
        return userRepository.findUnverifiedUsersCreatedBefore(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findInactiveUsers(LocalDateTime cutoffDate) {
        return userRepository.findInactiveUsersSince(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsersByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countUsersRegisteredBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsersByEmail(String emailPattern) {
        return userRepository.findByEmailContainingAndNotDeleted(emailPattern);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserActive(UUID userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        return user.isActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserDeleted(UUID userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        return user.isDeleted();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailVerified(UUID userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        return user.isEmailVerified();
    }

    // 유틸리티 메서드
    private String generateUniqueToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}