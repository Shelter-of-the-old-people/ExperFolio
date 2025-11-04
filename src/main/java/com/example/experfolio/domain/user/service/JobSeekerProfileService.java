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
    public JobSeekerProfile createProfile(UUID userId) {
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