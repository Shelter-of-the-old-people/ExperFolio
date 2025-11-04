package com.example.experfolio.domain.search.service;

import com.example.experfolio.domain.search.dto.SearchResponseDto;

/**
 * 검색 서비스 인터페이스
 */
public interface SearchService {

    /**
     * 검색 실행
     *
     * @param query 검색 쿼리
     * @return 검색 결과
     */
    SearchResponseDto search(String query);
}
