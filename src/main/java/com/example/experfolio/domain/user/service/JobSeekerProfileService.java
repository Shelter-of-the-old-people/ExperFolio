package com.example.experfolio.domain.user.service;

import com.example.experfolio.domain.user.entity.JobSeekerProfile;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.repository.JobSeekerProfileRepository;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobSeekerProfileService {

    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final UserService userService;

    // 프로필 생성 및 조회
    public JobSeekerProfile createProfile(UUID userId, String firstName, String lastName) {
        log.info("구직자 프로필 생성: userId={}", userId);
        
        // 사용자 존재 및 활성 상태 확인
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        // 이미 프로필이 존재하는지 확인
        if (jobSeekerProfileRepository.existsByUser(user)) {
            throw new BadRequestException("이미 구직자 프로필이 존재합니다");
        }
        
        JobSeekerProfile profile = JobSeekerProfile.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        
        JobSeekerProfile savedProfile = jobSeekerProfileRepository.save(profile);
        log.info("구직자 프로필 생성 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    @Transactional(readOnly = true)
    public Optional<JobSeekerProfile> findByUserId(UUID userId) {
        return jobSeekerProfileRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<JobSeekerProfile> findByUserEmail(String email) {
        return jobSeekerProfileRepository.findByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public JobSeekerProfile getByUserId(UUID userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("구직자 프로필을 찾을 수 없습니다: " + userId));
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return jobSeekerProfileRepository.existsByUserId(userId);
    }

    // 기본 정보 업데이트
    public JobSeekerProfile updateBasicInfo(UUID userId, String firstName, String lastName, 
                                          String phone, LocalDate birthDate, String gender) {
        log.info("구직자 기본 정보 업데이트: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.updateBasicInfo(firstName, lastName, phone, birthDate, gender);
        
        JobSeekerProfile savedProfile = jobSeekerProfileRepository.save(profile);
        log.info("구직자 기본 정보 업데이트 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    // 주소 정보 업데이트
    public JobSeekerProfile updateAddress(UUID userId, String address, String state, String postalCode) {
        log.info("구직자 주소 정보 업데이트: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.updateAddress(address, state, postalCode);
        
        return jobSeekerProfileRepository.save(profile);
    }

    // 프로필 정보 업데이트
    public JobSeekerProfile updateProfileInfo(UUID userId, String profileImageUrl, 
                                            String summary, String careerObjective) {
        log.info("구직자 프로필 정보 업데이트: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.updateProfileInfo(profileImageUrl, summary, careerObjective);
        
        return jobSeekerProfileRepository.save(profile);
    }

    // 희망 조건 업데이트
    public JobSeekerProfile updateDesiredConditions(UUID userId, String desiredPosition, 
                                                   Integer desiredSalaryMin, Integer desiredSalaryMax,
                                                   String desiredLocation, LocalDate availableStartDate) {
        log.info("구직자 희망 조건 업데이트: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.updateDesiredConditions(desiredPosition, desiredSalaryMin, desiredSalaryMax, 
                                      desiredLocation, availableStartDate);
        
        return jobSeekerProfileRepository.save(profile);
    }

    // URL 관리
    public JobSeekerProfile addUrl(UUID userId, String url) {
        log.info("구직자 URL 추가: userId={}, url={}", userId, url);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.addUrl(url);
        
        return jobSeekerProfileRepository.save(profile);
    }

    public JobSeekerProfile removeUrl(UUID userId, String url) {
        log.info("구직자 URL 제거: userId={}, url={}", userId, url);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.removeUrl(url);
        
        return jobSeekerProfileRepository.save(profile);
    }

    public JobSeekerProfile updateUrls(UUID userId, List<String> urls) {
        log.info("구직자 URL 목록 업데이트: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        profile.updateUrls(urls);
        
        return jobSeekerProfileRepository.save(profile);
    }

    // 검색 기능
    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findByDesiredPosition(String position) {
        return jobSeekerProfileRepository.findByDesiredPositionContainingIgnoreCase(position);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findByDesiredLocation(String location) {
        return jobSeekerProfileRepository.findByDesiredLocationContainingIgnoreCase(location);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findBySalaryRange(Integer minSalary, Integer maxSalary) {
        return jobSeekerProfileRepository.findBySalaryRange(minSalary, maxSalary);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findByAvailableStartDateBefore(LocalDate date) {
        return jobSeekerProfileRepository.findByAvailableStartDateBefore(date);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findByPositionAndLocation(String position, String location) {
        return jobSeekerProfileRepository.findByPositionAndLocation(position, location);
    }

    // RAG 검색을 위한 키워드 검색
    @Transactional(readOnly = true)
    public List<JobSeekerProfile> searchByKeyword(String keyword) {
        return jobSeekerProfileRepository.findByKeywordInProfileText(keyword);
    }

    // 복합 검색
    @Transactional(readOnly = true)
    public List<JobSeekerProfile> searchProfiles(String position, String location, 
                                                Integer minSalary, LocalDate maxStartDate) {
        return jobSeekerProfileRepository.findBySearchCriteria(position, location, minSalary, maxStartDate);
    }

    // 프로필 상태 관리
    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findCompleteProfiles() {
        return jobSeekerProfileRepository.findCompleteProfiles();
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findIncompleteProfiles() {
        return jobSeekerProfileRepository.findIncompleteProfiles();
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findActiveProfiles() {
        return jobSeekerProfileRepository.findActiveJobSeekerProfiles();
    }

    // 통계 기능
    @Transactional(readOnly = true)
    public long countByDesiredPosition(String position) {
        return jobSeekerProfileRepository.countByDesiredPosition(position);
    }

    @Transactional(readOnly = true)
    public long countByDesiredLocation(String location) {
        return jobSeekerProfileRepository.countByDesiredLocation(location);
    }

    // 프로필 완성도 확인
    @Transactional(readOnly = true)
    public boolean isProfileComplete(UUID userId) {
        JobSeekerProfile profile = getByUserId(userId);
        return profile.isProfileComplete();
    }

    // 전체 프로필 업데이트
    public JobSeekerProfile updateProfile(JobSeekerProfile profile) {
        JobSeekerProfile existingProfile = jobSeekerProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("프로필을 찾을 수 없습니다: " + profile.getId()));
        
        JobSeekerProfile savedProfile = jobSeekerProfileRepository.save(profile);
        log.info("구직자 프로필 업데이트 완료: profileId={}", savedProfile.getId());
        
        return savedProfile;
    }

    // 프로필 삭제 (사용자와 함께 삭제됨 - CASCADE)
    public void deleteProfile(UUID userId) {
        log.info("구직자 프로필 삭제: userId={}", userId);
        
        JobSeekerProfile profile = getByUserId(userId);
        jobSeekerProfileRepository.delete(profile);
        
        log.info("구직자 프로필 삭제 완료: userId={}", userId);
    }

    // 최신 프로필 목록 조회
    @Transactional(readOnly = true)
    public List<JobSeekerProfile> findAllOrderByCreatedAtDesc() {
        return jobSeekerProfileRepository.findAllOrderByCreatedAtDesc();
    }
}