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

    // 희망 직책으로 프로필 조회
    List<JobSeekerProfile> findByDesiredPosition(String desiredPosition);

    // 희망 직책 키워드로 프로필 검색
    List<JobSeekerProfile> findByDesiredPositionContainingIgnoreCase(String positionKeyword);

    // 희망 지역으로 프로필 조회
    List<JobSeekerProfile> findByDesiredLocation(String desiredLocation);

    // 희망 지역 키워드로 프로필 검색
    List<JobSeekerProfile> findByDesiredLocationContainingIgnoreCase(String locationKeyword);

    // 최소 희망연봉 이상인 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE jsp.desiredSalaryMin >= :minSalary")
    List<JobSeekerProfile> findByMinimumSalaryGreaterThanEqual(@Param("minSalary") Integer minSalary);

    // 최대 희망연봉 이하인 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE jsp.desiredSalaryMax <= :maxSalary")
    List<JobSeekerProfile> findByMaximumSalaryLessThanEqual(@Param("maxSalary") Integer maxSalary);

    // 연봉 범위 내 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "jsp.desiredSalaryMin >= :minSalary AND jsp.desiredSalaryMax <= :maxSalary")
    List<JobSeekerProfile> findBySalaryRange(@Param("minSalary") Integer minSalary, @Param("maxSalary") Integer maxSalary);

    // 특정 입사 가능일자로 프로필 조회
    List<JobSeekerProfile> findByAvailableStartDate(LocalDate availableDate);

    // 입사 가능일자가 특정 날짜 이전인 프로필 조회
    List<JobSeekerProfile> findByAvailableStartDateBefore(LocalDate cutoffDate);

    // 입사 가능일자가 특정 날짜 이후인 프로필 조회
    List<JobSeekerProfile> findByAvailableStartDateAfter(LocalDate startDate);

    // 입사 가능일자가 특정 기간 내인 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE jsp.availableStartDate BETWEEN :startDate AND :endDate")
    List<JobSeekerProfile> findByAvailableStartDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 직책과 지역으로 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "jsp.desiredPosition LIKE %:position% AND jsp.desiredLocation LIKE %:location%")
    List<JobSeekerProfile> findByPositionAndLocation(@Param("position") String position, @Param("location") String location);

    // 완성된 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "jsp.summary IS NOT NULL AND jsp.summary != '' AND " +
           "jsp.desiredPosition IS NOT NULL AND jsp.desiredPosition != ''")
    List<JobSeekerProfile> findCompleteProfiles();

    // 미완성 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "jsp.summary IS NULL OR jsp.summary = '' OR " +
           "jsp.desiredPosition IS NULL OR jsp.desiredPosition = ''")
    List<JobSeekerProfile> findIncompleteProfiles();

    // 프로필 텍스트에서 키워드로 검색 (RAG 검색용)
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "LOWER(jsp.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(jsp.careerObjective) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(jsp.desiredPosition) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<JobSeekerProfile> findByKeywordInProfileText(@Param("keyword") String keyword);

    // URL 패턴으로 프로필 검색
    @Query("SELECT jsp FROM JobSeekerProfile jsp JOIN jsp.urls url WHERE url LIKE %:urlPattern%")
    List<JobSeekerProfile> findByUrlContaining(@Param("urlPattern") String urlPattern);

    // 주/도별 프로필 조회
    List<JobSeekerProfile> findByState(String state);

    // 주/도 키워드로 프로필 검색
    List<JobSeekerProfile> findByStateContainingIgnoreCase(String stateKeyword);

    // 희망 직책별 프로필 수 조회
    @Query("SELECT COUNT(jsp) FROM JobSeekerProfile jsp WHERE jsp.desiredPosition = :position")
    long countByDesiredPosition(@Param("position") String position);

    // 희망 지역별 프로필 수 조회
    @Query("SELECT COUNT(jsp) FROM JobSeekerProfile jsp WHERE jsp.desiredLocation = :location")
    long countByDesiredLocation(@Param("location") String location);

    // 모든 프로필을 최신 순으로 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp ORDER BY jsp.createdAt DESC")
    List<JobSeekerProfile> findAllOrderByCreatedAtDesc();

    // 복합 검색 조건으로 프로필 조회
    @Query("SELECT jsp FROM JobSeekerProfile jsp WHERE " +
           "jsp.desiredPosition LIKE %:position% AND " +
           "jsp.desiredLocation LIKE %:location% AND " +
           "jsp.desiredSalaryMin >= :minSalary AND " +
           "jsp.availableStartDate <= :maxStartDate")
    List<JobSeekerProfile> findBySearchCriteria(
        @Param("position") String position,
        @Param("location") String location,
        @Param("minSalary") Integer minSalary,
        @Param("maxStartDate") LocalDate maxStartDate
    );
}