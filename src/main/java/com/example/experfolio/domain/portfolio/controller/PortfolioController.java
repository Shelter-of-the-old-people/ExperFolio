package com.example.experfolio.domain.portfolio.controller;

import com.example.experfolio.domain.portfolio.dto.BasicInfoDto;
import com.example.experfolio.domain.portfolio.dto.PortfolioItemDto;
import com.example.experfolio.domain.portfolio.dto.PortfolioResponseDto;
import com.example.experfolio.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 포트폴리오 관리 컨트롤러
 * Portfolio.txt Use Case 기반으로 구성
 *
 * NOTE: userDetails.getUsername()은 JWT의 userId claim (UUID 문자열)을 반환합니다.
 */
@Tag(name = "Portfolio", description = "포트폴리오 관리 API")
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 1.1 포트폴리오 생성
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "포트폴리오 생성", description = "구직자가 새로운 포트폴리오를 생성합니다.")
    @PostMapping
    public ResponseEntity<PortfolioResponseDto> createPortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BasicInfoDto basicInfoDto
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.createPortfolio(userId, basicInfoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2.1 포트폴리오 전체 조회
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "내 포트폴리오 조회", description = "본인의 포트폴리오 전체 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<PortfolioResponseDto> getMyPortfolio(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.getMyPortfolio(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 2.2 BasicInfo 수정
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "기본정보 수정", description = "포트폴리오의 기본 정보를 수정합니다.")
    @PutMapping("/basic-info")
    public ResponseEntity<PortfolioResponseDto> updateBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BasicInfoDto basicInfoDto
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.updateBasicInfo(userId, basicInfoDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 3.1 포트폴리오 아이템 추가 (최대 5개)
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 추가", description = "프로젝트/활동/연구 등 아이템을 추가합니다. (최대 5개)")
    @PostMapping("/items")
    public ResponseEntity<PortfolioResponseDto> addPortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart(value = "item") PortfolioItemDto portfolioItemDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.addPortfolioItem(userId, portfolioItemDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3.2 포트폴리오 아이템 수정
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 수정", description = "기존 포트폴리오 아이템을 수정합니다.")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<PortfolioResponseDto> updatePortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemId,
            @Valid @RequestPart(value = "item") PortfolioItemDto portfolioItemDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.updatePortfolioItem(userId, itemId, portfolioItemDto, files);
        return ResponseEntity.ok(response);
    }

    /**
     * 3.3 포트폴리오 아이템 삭제
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 삭제", description = "특정 포트폴리오 아이템을 삭제합니다.")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deletePortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemId
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        portfolioService.deletePortfolioItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 3.4 포트폴리오 아이템 순서 변경
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 순서 변경", description = "포트폴리오 아이템의 순서를 재배치합니다.")
    @PutMapping("/items/reorder")
    public ResponseEntity<PortfolioResponseDto> reorderPortfolioItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<String> itemIds
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        PortfolioResponseDto response = portfolioService.reorderPortfolioItems(userId, itemIds);
        return ResponseEntity.ok(response);
    }

    /**
     * 6.1 포트폴리오 전체 삭제
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 완전히 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<?> deletePortfolio(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = userDetails.getUsername(); // UUID 문자열
        portfolioService.deletePortfolio(userId);
        return ResponseEntity.noContent().build();
    }
}