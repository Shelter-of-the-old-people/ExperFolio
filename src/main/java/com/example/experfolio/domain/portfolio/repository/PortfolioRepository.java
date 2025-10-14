package com.example.experfolio.domain.portfolio.repository;

import com.example.experfolio.domain.portfolio.document.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 포트폴리오 MongoDB Repository
 */
@Repository
public interface PortfolioRepository extends MongoRepository<Portfolio, String> {

    /**
     * userId로 포트폴리오 조회
     */
    Optional<Portfolio> findByUserId(String userId);

    /**
     * userId로 포트폴리오 존재 여부 확인
     */
    boolean existsByUserId(String userId);

    /**
     * userId로 포트폴리오 삭제
     */
    void deleteByUserId(String userId);
}
