package com.example.experfolio.domain.favorite.repository;

import com.example.experfolio.domain.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 즐겨찾기 리포지토리
 */
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    /**
     * 리크루터와 구직자로 즐겨찾기 찾기
     */
    Optional<Favorite> findByRecruiterIdAndJobSeekerId(String recruiterId, String jobSeekerId);

    /**
     * 리크루터와 구직자로 즐겨찾기 존재 여부 확인
     */
    boolean existsByRecruiterIdAndJobSeekerId(String recruiterId, String jobSeekerId);

    /**
     * 리크루터의 즐겨찾기 목록 조회 (페이징)
     */
    @Query("SELECT f FROM Favorite f " +
           "WHERE f.recruiterId = :recruiterId " +
           "ORDER BY f.createdAt DESC")
    Page<Favorite> findByRecruiterId(@Param("recruiterId") String recruiterId, Pageable pageable);

    /**
     * 리크루터와 구직자로 즐겨찾기 삭제
     */
    void deleteByRecruiterIdAndJobSeekerId(String recruiterId, String jobSeekerId);
}
