package com.example.experfolio.domain.user.repository;

import com.example.experfolio.domain.user.entity.RecruiterProfile;
import com.example.experfolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, UUID> {

    // 사용자 객체로 프로필 조회
    Optional<RecruiterProfile> findByUser(User user);

    // 사용자 ID로 프로필 조회
    Optional<RecruiterProfile> findByUserId(UUID userId);

    // 사용자 이메일로 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE rp.user.email = :email")
    Optional<RecruiterProfile> findByUserEmail(@Param("email") String email);

    // 사용자의 프로필 존재 여부 확인
    boolean existsByUser(User user);

    // 사용자 ID의 프로필 존재 여부 확인
    boolean existsByUserId(UUID userId);

    // 회사명으로 프로필 조회
    List<RecruiterProfile> findByCompanyName(String companyName);

    // 회사명 키워드로 프로필 검색
    List<RecruiterProfile> findByCompanyNameContainingIgnoreCase(String companyKeyword);

    // 부서별 프로필 조회
    List<RecruiterProfile> findByDepartment(String department);

    // 부서 키워드로 프로필 검색
    List<RecruiterProfile> findByDepartmentContainingIgnoreCase(String departmentKeyword);

    // 직책별 프로필 조회
    List<RecruiterProfile> findByPosition(String position);

    // 직책 키워드로 프로필 검색
    List<RecruiterProfile> findByPositionContainingIgnoreCase(String positionKeyword);

    // 키워드로 프로필 전체 검색
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "LOWER(rp.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rp.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rp.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rp.companyDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RecruiterProfile> findByKeyword(@Param("keyword") String keyword);

    // 회사 인증 상태별 프로필 조회
    List<RecruiterProfile> findByIsCompanyVerified(Boolean isVerified);

    // 사업자등록번호로 프로필 조회
    List<RecruiterProfile> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    // 사업자등록번호 존재 여부 확인
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    // 회사명과 사업자등록번호로 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.companyName = :companyName AND rp.businessRegistrationNumber = :businessRegNumber")
    List<RecruiterProfile> findByCompanyNameAndBusinessRegistrationNumber(
        @Param("companyName") String companyName, 
        @Param("businessRegNumber") String businessRegistrationNumber
    );

    // 완성된 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.firstName IS NOT NULL AND rp.firstName != '' AND " +
           "rp.lastName IS NOT NULL AND rp.lastName != '' AND " +
           "rp.companyName IS NOT NULL AND rp.companyName != ''")
    List<RecruiterProfile> findCompleteProfiles();

    // 미완성 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.firstName IS NULL OR rp.firstName = '' OR " +
           "rp.lastName IS NULL OR rp.lastName = '' OR " +
           "rp.companyName IS NULL OR rp.companyName = ''")
    List<RecruiterProfile> findIncompleteProfiles();

    // 인증 서류가 있는 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.businessRegistrationNumber IS NOT NULL OR " +
           "rp.companyVerificationDocumentUrl IS NOT NULL")
    List<RecruiterProfile> findProfilesWithVerificationDocuments();

    // 인증 서류가 없는 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.businessRegistrationNumber IS NULL AND " +
           "rp.companyVerificationDocumentUrl IS NULL")
    List<RecruiterProfile> findProfilesWithoutVerificationDocuments();

    // 회사명별 프로필 수 조회
    @Query("SELECT COUNT(rp) FROM RecruiterProfile rp WHERE rp.companyName = :companyName")
    long countByCompanyName(@Param("companyName") String companyName);

    // 인증 상태별 프로필 수 조회
    @Query("SELECT COUNT(rp) FROM RecruiterProfile rp WHERE rp.isCompanyVerified = :isVerified")
    long countByVerificationStatus(@Param("isVerified") Boolean isVerified);

    // 모든 고유한 회사명 조회
    @Query("SELECT DISTINCT rp.companyName FROM RecruiterProfile rp WHERE rp.companyName IS NOT NULL ORDER BY rp.companyName")
    List<String> findDistinctCompanyNames();

    // 모든 고유한 부서명 조회
    @Query("SELECT DISTINCT rp.department FROM RecruiterProfile rp WHERE rp.department IS NOT NULL ORDER BY rp.department")
    List<String> findDistinctDepartments();

    // 모든 고유한 직책명 조회
    @Query("SELECT DISTINCT rp.position FROM RecruiterProfile rp WHERE rp.position IS NOT NULL ORDER BY rp.position")
    List<String> findDistinctPositions();

    // 프로필의 인증 상태 업데이트
    @Modifying
    @Query("UPDATE RecruiterProfile rp SET rp.isCompanyVerified = :isVerified WHERE rp.id = :profileId")
    void updateVerificationStatus(@Param("profileId") UUID profileId, @Param("isVerified") Boolean isVerified);

    // 사업자등록번호로 회사 일괄 인증
    @Modifying
    @Query("UPDATE RecruiterProfile rp SET rp.isCompanyVerified = true WHERE rp.businessRegistrationNumber = :businessRegNumber")
    void verifyCompaniesByBusinessRegistrationNumber(@Param("businessRegNumber") String businessRegistrationNumber);

    // 모든 프로필을 최신 순으로 조회
    @Query("SELECT rp FROM RecruiterProfile rp ORDER BY rp.createdAt DESC")
    List<RecruiterProfile> findAllOrderByCreatedAtDesc();

    // 복합 검색 조건으로 프로필 조회
    @Query("SELECT rp FROM RecruiterProfile rp WHERE " +
           "rp.companyName LIKE %:companyName% AND " +
           "rp.department LIKE %:department% AND " +
           "rp.isCompanyVerified = :isVerified")
    List<RecruiterProfile> findBySearchCriteria(
        @Param("companyName") String companyName,
        @Param("department") String department,
        @Param("isVerified") Boolean isVerified
    );
}