package com.example.experfolio.domain.favorite.service;

import com.example.experfolio.domain.favorite.dto.FavoriteExistsDto;
import com.example.experfolio.domain.favorite.dto.FavoriteResponseDto;
import com.example.experfolio.domain.favorite.dto.JobSeekerInfoDto;
import com.example.experfolio.domain.favorite.entity.Favorite;
import com.example.experfolio.domain.favorite.exception.DuplicateFavoriteException;
import com.example.experfolio.domain.favorite.exception.FavoriteNotFoundException;
import com.example.experfolio.domain.favorite.repository.FavoriteRepository;
import com.example.experfolio.domain.portfolio.document.Portfolio;
import com.example.experfolio.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 즐겨찾기 서비스 구현체
 * Portfolio 중심 설계로 PortfolioRepository를 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public FavoriteResponseDto addFavorite(String recruiterId, String jobSeekerId) {
        log.info("Adding favorite: recruiterId={}, jobSeekerId={}", recruiterId, jobSeekerId);

        // 중복 체크
        if (favoriteRepository.existsByRecruiterIdAndJobSeekerId(recruiterId, jobSeekerId)) {
            throw new DuplicateFavoriteException("이미 즐겨찾기한 구직자입니다");
        }

        // Portfolio 존재 확인 (구직자의 포트폴리오가 있는지 확인)
        Portfolio portfolio = portfolioRepository.findByUserId(jobSeekerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 구직자의 포트폴리오를 찾을 수 없습니다"));

        // Favorite 엔티티 생성
        Favorite favorite = Favorite.builder()
                .recruiterId(recruiterId)
                .jobSeekerId(jobSeekerId)
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("Favorite added successfully: id={}", savedFavorite.getId());

        return convertToResponseDto(savedFavorite);
    }

    @Override
    @Transactional
    public void removeFavorite(String recruiterId, String jobSeekerId) {
        log.info("Removing favorite: recruiterId={}, jobSeekerId={}", recruiterId, jobSeekerId);

        // 즐겨찾기 존재 확인
        Favorite favorite = favoriteRepository.findByRecruiterIdAndJobSeekerId(recruiterId, jobSeekerId)
                .orElseThrow(() -> new FavoriteNotFoundException("즐겨찾기를 찾을 수 없습니다"));

        favoriteRepository.delete(favorite);
        log.info("Favorite removed successfully: id={}", favorite.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponseDto> getFavorites(String recruiterId, Pageable pageable) {
        log.info("Getting favorites: recruiterId={}, page={}", recruiterId, pageable.getPageNumber());

        Page<Favorite> favoritePage = favoriteRepository.findByRecruiterId(recruiterId, pageable);

        return favoritePage.map(this::convertToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteExistsDto checkFavoriteExists(String recruiterId, String jobSeekerId) {
        log.info("Checking favorite exists: recruiterId={}, jobSeekerId={}", recruiterId, jobSeekerId);

        return favoriteRepository.findByRecruiterIdAndJobSeekerId(recruiterId, jobSeekerId)
                .map(favorite -> FavoriteExistsDto.builder()
                        .exists(true)
                        .favoriteId(favorite.getId().toString())
                        .createdAt(favorite.getCreatedAt())
                        .build())
                .orElse(FavoriteExistsDto.builder()
                        .exists(false)
                        .build());
    }

    /**
     * Favorite -> FavoriteResponseDto 변환
     * Portfolio에서 구직자 정보 가져오기
     */
    private FavoriteResponseDto convertToResponseDto(Favorite favorite) {
        String jobSeekerId = favorite.getJobSeekerId();
        JobSeekerInfoDto jobSeekerInfo = buildJobSeekerInfo(jobSeekerId);

        return FavoriteResponseDto.builder()
                .id(favorite.getId().toString())
                .jobSeeker(jobSeekerInfo)
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * MongoDB Portfolio에서 구직자 정보 가져오기
     */
    private JobSeekerInfoDto buildJobSeekerInfo(String jobSeekerId) {
        // MongoDB에서 포트폴리오 정보 조회
        Portfolio portfolio = portfolioRepository.findByUserId(jobSeekerId)
                .orElse(null);

        if (portfolio != null && portfolio.getBasicInfo() != null) {
            // 포트폴리오가 있는 경우 - MongoDB에서 정보 사용
            return JobSeekerInfoDto.builder()
                    .id(jobSeekerId)
                    .name(portfolio.getBasicInfo().getName())
                    .desiredPosition(portfolio.getBasicInfo().getDesiredPosition())
                    .major(portfolio.getBasicInfo().getMajor())
                    .schoolName(portfolio.getBasicInfo().getSchoolName())
                    .gpa(portfolio.getBasicInfo().getGpa())
                    .build();
        } else {
            // 포트폴리오가 없는 경우 - 기본 정보만 반환
            return JobSeekerInfoDto.builder()
                    .id(jobSeekerId)
                    .name("정보 없음")
                    .desiredPosition(null)
                    .major(null)
                    .schoolName(null)
                    .gpa(null)
                    .build();
        }
    }
}
