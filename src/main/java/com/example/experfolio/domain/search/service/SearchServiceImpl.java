package com.example.experfolio.domain.search.service;

import com.example.experfolio.domain.search.dto.SearchRequestDto;
import com.example.experfolio.domain.search.dto.SearchResponseDto;
import com.example.experfolio.domain.search.exception.SearchServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * 검색 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8001}")
    private String aiServerUrl;

    @Value("${ai.server.search-endpoint:/ai/search}")
    private String searchEndpoint;

    @Override
    public SearchResponseDto search(String query) {
        log.info("Executing search with query: {}", maskQuery(query));

        try {
            // AI 서버 URL 구성
            String url = aiServerUrl + searchEndpoint;
            log.debug("AI server URL: {}", url);

            // 요청 생성
            SearchRequestDto requestDto = SearchRequestDto.builder()
                    .query(query)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SearchRequestDto> request = new HttpEntity<>(requestDto, headers);

            // AI 서버로 요청 전송
            long startTime = System.currentTimeMillis();
            ResponseEntity<SearchResponseDto> response = restTemplate.postForEntity(
                    url,
                    request,
                    SearchResponseDto.class
            );
            long endTime = System.currentTimeMillis();

            log.info("Search completed in {}ms, total results: {}",
                    (endTime - startTime),
                    response.getBody() != null ? response.getBody().getTotalResults() : 0);

            return response.getBody();

        } catch (ResourceAccessException e) {
            log.error("Failed to connect to AI server: {}", e.getMessage());
            throw new SearchServiceException("AI 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.", e);

        } catch (HttpClientErrorException e) {
            log.error("AI server returned client error: {} - {}", e.getStatusCode(), e.getMessage());
            throw new SearchServiceException("검색 요청이 잘못되었습니다: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.error("AI server returned server error: {} - {}", e.getStatusCode(), e.getMessage());
            throw new SearchServiceException("AI 서버에서 오류가 발생했습니다: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Unexpected error during search: {}", e.getMessage(), e);
            throw new SearchServiceException("검색 중 예상치 못한 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 로깅을 위해 쿼리를 마스킹 (개인정보 보호)
     *
     * @param query 원본 쿼리
     * @return 마스킹된 쿼리
     */
    private String maskQuery(String query) {
        if (query == null || query.length() <= 20) {
            return query;
        }
        return query.substring(0, 20) + "...";
    }
}
