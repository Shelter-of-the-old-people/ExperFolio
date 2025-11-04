package com.example.experfolio.domain.user.repository;

import com.example.experfolio.domain.user.entity.JobSeekerProfile;
import com.example.experfolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, UUID> {

    // 사용자 객체로 프로필 조회
    Optional<JobSeekerProfile> findByUser(User user);

    // 사용자 ID로 프로필 조회
    Optional<JobSeekerProfile> findByUserId(UUID userId);

    // 사용자 이메일로 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE jsp.user.email = :email")
    Optional<JobSeekerProfile> findByUserEmail(@Param("email") String email);

    // 사용자의 프로필 존재 여부 확인
    boolean existsByUser(User user);

    // 사용자 ID의 프로필 존재 여부 확인
    boolean existsByUserId(UUID userId);

    // 활성 사용자의 프로필만 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE jsp.user.deletedAt IS NULL")
    List<JobSeekerProfile> findActiveJobSeekerProfiles();

    // 모든 프로필을 최신 순으로 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp ORDER BY jsp.createdAt DESC")
    List<JobSeekerProfile> findAllOrderByCreatedAtDesc();

}