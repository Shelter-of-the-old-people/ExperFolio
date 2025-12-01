package com.example.experfolio.domain.search.service;

import com.example.experfolio.domain.portfolio.document.BasicInfo;
import com.example.experfolio.domain.portfolio.document.Portfolio;
import com.example.experfolio.domain.portfolio.repository.PortfolioRepository;
import com.example.experfolio.domain.search.dto.CandidateDto;
import com.example.experfolio.domain.search.dto.SearchRequestDto;
import com.example.experfolio.domain.search.dto.SearchResponseDto;
import com.example.experfolio.domain.search.dto.UserInfoDto;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 검색 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final RestTemplate restTemplate;
    private final PortfolioRepository portfolioRepository;

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

            log.info("Sending request: {}", request);

            // AI 서버로 요청 전송
            long startTime = System.currentTimeMillis();
            ResponseEntity<SearchResponseDto> response = restTemplate.postForEntity(
                    url,
                    request,
                    SearchResponseDto.class
            );
            log.info("Search response: {}", response.getBody());
            long endTime = System.currentTimeMillis();

            log.info("Search completed in {}ms, total results: {}",
                    (endTime - startTime),
                    response.getBody() != null ? response.getBody().getTotalResults() : 0);

            // 응답에 userInfo 추가
            SearchResponseDto searchResponse = response.getBody();
            if (searchResponse != null && searchResponse.getCandidates() != null) {
                enrichCandidatesWithUserInfo(searchResponse.getCandidates());
            }

            return searchResponse;

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
     * 후보자 목록에 포트폴리오 기본 정보 추가
     *
     * @param candidates 후보자 목록
     */
    private void enrichCandidatesWithUserInfo(List<CandidateDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        log.debug("Enriching {} candidates with user info from portfolio", candidates.size());

        // Step 1: 모든 userId 수집
        List<String> userIds = candidates.stream()
                .map(CandidateDto::getUserId)
                .collect(Collectors.toList());

        // Step 2: 배치로 포트폴리오 조회
        List<Portfolio> portfolios = portfolioRepository.findByUserIdIn(userIds);
        log.debug("Found {} portfolios for {} user IDs", portfolios.size(), userIds.size());

        // Step 3: userId -> Portfolio 맵 생성 (빠른 조회)
        Map<String, Portfolio> portfolioMap = portfolios.stream()
                .collect(Collectors.toMap(Portfolio::getUserId, p -> p));

        // Step 4: 각 candidate에 userInfo 추가
        for (CandidateDto candidate : candidates) {
            Portfolio portfolio = portfolioMap.get(candidate.getUserId());

            if (portfolio != null && portfolio.getBasicInfo() != null) {
                BasicInfo basicInfo = portfolio.getBasicInfo();

                UserInfoDto userInfo = UserInfoDto.builder()
                        .name(basicInfo.getName())
                        .schoolName(basicInfo.getSchoolName())
                        .gpa(basicInfo.getGpa())
                        .major(basicInfo.getMajor())
                        .awardsCount(basicInfo.getAwards() != null ? basicInfo.getAwards().size() : 0)
                        .build();

                candidate.setUserInfo(userInfo);
                log.trace("Added user info for userId: {}", candidate.getUserId());
            } else {
                // Portfolio가 없거나 BasicInfo가 없는 경우
                candidate.setUserInfo(null);
                log.debug("No portfolio or basicInfo found for userId: {}", candidate.getUserId());
            }
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
