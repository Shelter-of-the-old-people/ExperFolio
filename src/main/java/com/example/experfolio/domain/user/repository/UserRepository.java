package com.example.experfolio.domain.user.repository;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 삭제되지 않은 사용자를 이메일로 조회
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 삭제되지 않은 사용자의 이메일 존재 여부 확인
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // 이메일 인증 토큰으로 사용자 조회
    Optional<User> findByEmailVerificationToken(String token);

    // 비밀번호 재설정 토큰으로 사용자 조회
    Optional<User> findByPasswordResetToken(String token);

    // 유효한 비밀번호 재설정 토큰으로 사용자 조회
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // 역할별 사용자 목록 조회
    List<User> findByRole(UserRole role);

    // 상태별 사용자 목록 조회
    List<User> findByStatus(UserStatus status);

    // 역할과 상태로 사용자 목록 조회
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    // 삭제되지 않은 활성 사용자를 역할과 상태로 조회
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND u.deletedAt IS NULL")
    List<User> findActiveUsersByRoleAndStatus(@Param("role") UserRole role, @Param("status") UserStatus status);

    // 특정 날짜 이전에 생성되고 이메일 미인증 사용자 조회
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersCreatedBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 특정 날짜 이후 로그인하지 않은 사용자 조회
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate AND u.deletedAt IS NULL")
    List<User> findInactiveUsersSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 역할별 활성 사용자 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    long countActiveUsersByRole(@Param("role") UserRole role);

    // 특정 기간 내 가입한 사용자 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 사용자의 마지막 로그인 시간 업데이트
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    // 토큰으로 이메일 인증 처리
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = null WHERE u.emailVerificationToken = :token")
    void verifyEmailByToken(@Param("token") String token);

    // 사용자 상태 업데이트
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") UUID userId, @Param("status") UserStatus status);

    // 삭제되지 않은 모든 사용자를 최신 순으로 조회
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsersOrderByCreatedAtDesc();

    // 이메일 패턴으로 삭제되지 않은 사용자 검색
    @Query("SELECT u FROM User u WHERE u.email LIKE %:emailPattern% AND u.deletedAt IS NULL")
    List<User> findByEmailContainingAndNotDeleted(@Param("emailPattern") String emailPattern);
}