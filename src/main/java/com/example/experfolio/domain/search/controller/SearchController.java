package com.example.experfolio.domain.search.controller;

import com.example.experfolio.domain.search.dto.SearchRequestDto;
import com.example.experfolio.domain.search.dto.SearchResponseDto;
import com.example.experfolio.domain.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 API")
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(
            summary = "후보자 검색",
            description = "AI 기반 후보자 검색을 수행합니다. RECRUITER 역할만 사용 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (쿼리가 비어있거나 유효하지 않음)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT 토큰이 없거나 유효하지 않음)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (RECRUITER 역할이 아님)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (AI 서버 연결 실패 또는 내부 오류)"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "서비스 이용 불가 (AI 서버가 응답하지 않음)"
            )
    })
    public ResponseEntity<SearchResponseDto> search(
            @Valid @RequestBody SearchRequestDto request
    ) {
        log.info("Search request received with query length: {}", request.getQuery().length());

        SearchResponseDto response = searchService.search(request.getQuery());

        return ResponseEntity.ok(response);
    }
}
