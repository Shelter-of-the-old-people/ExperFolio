package com.example.experfolio.domain.user.service;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    
    // 사용자 생성 및 등록
    User createUser(String email, String password, UserRole role);
    
    // 사용자 조회
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findActiveByEmail(String email);
    
    // 사용자 인증 관련
    boolean existsByEmail(String email);
    boolean isEmailAvailable(String email);
    Optional<User> findByPasswordResetToken(String token);
    Optional<User> findByValidPasswordResetToken(String token);

    // 비밀번호 관리
    void updatePassword(UUID userId, String newPassword);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    String generatePasswordResetToken(UUID userId);
    boolean isPasswordResetTokenValid(String token);
    
    // 로그인 관리
    void updateLastLoginTime(UUID userId);
    void updateLastLoginTime(UUID userId, LocalDateTime loginTime);
    
    // 사용자 정보 업데이트
    User updateUser(User user);
    User updateUserInfo(UUID userId, String name, String phoneNumber);
    User updateEmail(UUID userId, String newEmail);
    void deleteUser(UUID userId, String password);
    void softDeleteUser(UUID userId);
    
    // 사용자 목록 조회
    List<User> findByRole(UserRole role);
    List<User> findActiveUsersByRole(UserRole role);
    List<User> findAllActiveUsers();
    
    // 관리자 기능
    List<User> findUnverifiedUsers(LocalDateTime cutoffDate);
    List<User> findInactiveUsers(LocalDateTime cutoffDate);
    long countUsersByRole(UserRole role);
    long countUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 검색 기능
    List<User> searchUsersByEmail(String emailPattern);

    // 유틸리티 메서드
    boolean isUserDeleted(UUID userId);
}