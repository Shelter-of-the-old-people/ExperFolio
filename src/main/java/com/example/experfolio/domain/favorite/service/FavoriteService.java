package com.example.experfolio.domain.favorite.service;

import com.example.experfolio.domain.favorite.dto.FavoriteExistsDto;
import com.example.experfolio.domain.favorite.dto.FavoriteResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 즐겨찾기 서비스 인터페이스 (MVP)
 */
public interface FavoriteService {

    /**
     * 즐겨찾기 추가
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @return 생성된 즐겨찾기 정보
     * @throws com.example.experfolio.domain.favorite.exception.DuplicateFavoriteException 중복 즐겨찾기
     * @throws IllegalArgumentException 유효하지 않은 사용자
     */
    FavoriteResponseDto addFavorite(String recruiterId, String jobSeekerId);

    /**
     * 즐겨찾기 제거
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @throws com.example.experfolio.domain.favorite.exception.FavoriteNotFoundException 즐겨찾기가 존재하지 않음
     */
    void removeFavorite(String recruiterId, String jobSeekerId);

    /**
     * 즐겨찾기 목록 조회 (페이징)
     * @param recruiterId 리크루터 ID
     * @param pageable 페이징 정보
     * @return 즐겨찾기 목록
     */
    Page<FavoriteResponseDto> getFavorites(String recruiterId, Pageable pageable);

    /**
     * 즐겨찾기 여부 확인
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @return 즐겨찾기 존재 여부 및 정보
     */
    FavoriteExistsDto checkFavoriteExists(String recruiterId, String jobSeekerId);
}
