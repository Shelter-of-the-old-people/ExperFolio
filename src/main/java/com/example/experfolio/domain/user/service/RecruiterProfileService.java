package com.example.experfolio.domain.user.service;

import com.example.experfolio.domain.user.entity.RecruiterProfile;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.repository.RecruiterProfileRepository;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final UserService userService;

    // 프로필 생성 및 조회
    public RecruiterProfile createProfile(UUID userId, String firstName, String lastName, String companyName) {
        log.info("채용담당자 프로필 생성: userId={}, companyName={}", userId, companyName);
        
        // 사용자 존재 및 활성 상태 확인
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        // 이미 프로필이 존재하는지 확인
        if (recruiterProfileRepository.existsByUser(user)) {
            throw new BadRequestException("이미 채용담당자 프로필이 존재합니다");
        }
        
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .companyName(companyName)
                .build();
        
        RecruiterProfile savedProfile = recruiterProfileRepository.save(profile);
        log.info("채용담당자 프로필 생성 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    @Transactional(readOnly = true)
    public Optional<RecruiterProfile> findByUserId(UUID userId) {
        return recruiterProfileRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<RecruiterProfile> findByUserEmail(String email) {
        return recruiterProfileRepository.findByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public RecruiterProfile getByUserId(UUID userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("채용담당자 프로필을 찾을 수 없습니다: " + userId));
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return recruiterProfileRepository.existsByUserId(userId);
    }

    // 기본 정보 업데이트
    public RecruiterProfile updateBasicInfo(UUID userId, String firstName, String lastName, String phone) {
        log.info("채용담당자 기본 정보 업데이트: userId={}", userId);
        
        RecruiterProfile profile = getByUserId(userId);
        profile.updateBasicInfo(firstName, lastName, phone);
        
        RecruiterProfile savedProfile = recruiterProfileRepository.save(profile);
        log.info("채용담당자 기본 정보 업데이트 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    // 인증 정보 업데이트
    public RecruiterProfile updateVerificationInfo(UUID userId, String businessRegistrationNumber,
                                                 String companyVerificationDocumentUrl) {
        log.info("채용담당자 인증 정보 업데이트: userId={}", userId);
        
        RecruiterProfile profile = getByUserId(userId);
        profile.updateVerificationInfo(businessRegistrationNumber, companyVerificationDocumentUrl);
        
        return recruiterProfileRepository.save(profile);
    }

    // 회사 인증 관리
    public RecruiterProfile verifyCompany(UUID userId) {
        log.info("회사 인증 처리: userId={}", userId);
        
        RecruiterProfile profile = getByUserId(userId);
        
        if (!profile.hasVerificationDocuments()) {
            throw new BadRequestException("인증에 필요한 서류가 없습니다");
        }
        
        profile.verifyCompany();
        RecruiterProfile savedProfile = recruiterProfileRepository.save(profile);
        
        log.info("회사 인증 완료: userId={}, companyName={}", userId, profile.getCompanyName());
        
        return savedProfile;
    }

    // 회사별 인증 관리 (관리자 기능)
    public void verifyCompaniesByBusinessRegistrationNumber(String businessRegistrationNumber) {
        log.info("사업자등록번호로 회사 일괄 인증: businessRegNumber={}", businessRegistrationNumber);
        
        if (!recruiterProfileRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber)) {
            throw new ResourceNotFoundException("해당 사업자등록번호의 프로필이 없습니다: " + businessRegistrationNumber);
        }
        
        recruiterProfileRepository.verifyCompaniesByBusinessRegistrationNumber(businessRegistrationNumber);
        log.info("사업자등록번호로 회사 일괄 인증 완료: businessRegNumber={}", businessRegistrationNumber);
    }

    public void updateVerificationStatus(UUID profileId, Boolean isVerified) {
        log.info("회사 인증 상태 업데이트: profileId={}, isVerified={}", profileId, isVerified);
        
        recruiterProfileRepository.updateVerificationStatus(profileId, isVerified);
        log.info("회사 인증 상태 업데이트 완료: profileId={}", profileId);
    }

    // 프로필 상태 관리
    @Transactional(readOnly = true)
    public List<RecruiterProfile> findCompleteProfiles() {
        return recruiterProfileRepository.findCompleteProfiles();
    }

    @Transactional(readOnly = true)
    public List<RecruiterProfile> findIncompleteProfiles() {
        return recruiterProfileRepository.findIncompleteProfiles();
    }

    // 통계 기능
    @Transactional(readOnly = true)
    public long countByCompanyName(String companyName) {
        return recruiterProfileRepository.countByCompanyName(companyName);
    }

    @Transactional(readOnly = true)
    public long countByVerificationStatus(Boolean isVerified) {
        return recruiterProfileRepository.countByVerificationStatus(isVerified);
    }

    // 고유 데이터 조회 (통계 및 필터링용)
    @Transactional(readOnly = true)
    public List<String> findAllCompanyNames() {
        return recruiterProfileRepository.findDistinctCompanyNames();
    }

    @Transactional(readOnly = true)
    public List<String> findAllDepartments() {
        return recruiterProfileRepository.findDistinctDepartments();
    }

    @Transactional(readOnly = true)
    public List<String> findAllPositions() {
        return recruiterProfileRepository.findDistinctPositions();
    }

    // 복합 검색
    @Transactional(readOnly = true)
    public List<RecruiterProfile> searchProfiles(String companyName, String department, Boolean isVerified) {
        return recruiterProfileRepository.findBySearchCriteria(companyName, department, isVerified);
    }

    // 프로필 완성도 확인
    @Transactional(readOnly = true)
    public boolean isProfileComplete(UUID userId) {
        RecruiterProfile profile = getByUserId(userId);
        return profile.isProfileComplete();
    }

    @Transactional(readOnly = true)
    public boolean hasVerificationDocuments(UUID userId) {
        RecruiterProfile profile = getByUserId(userId);
        return profile.hasVerificationDocuments();
    }

    // 전체 프로필 업데이트
    public RecruiterProfile updateProfile(RecruiterProfile profile) {
        RecruiterProfile existingProfile = recruiterProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("프로필을 찾을 수 없습니다: " + profile.getId()));
        
        RecruiterProfile savedProfile = recruiterProfileRepository.save(profile);
        log.info("채용담당자 프로필 업데이트 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    // 프로필 삭제 (사용자와 함께 삭제됨 - CASCADE)
    public void deleteProfile(UUID userId) {
        log.info("채용담당자 프로필 삭제: userId={}", userId);
        
        RecruiterProfile profile = getByUserId(userId);
        recruiterProfileRepository.delete(profile);
        
        log.info("채용담당자 프로필 삭제 완료: userId={}", userId);
    }

    // 최신 프로필 목록 조회
    @Transactional(readOnly = true)
    public List<RecruiterProfile> findAllOrderByCreatedAtDesc() {
        return recruiterProfileRepository.findAllOrderByCreatedAtDesc();
    }
}