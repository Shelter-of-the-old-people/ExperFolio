package com.example.experfolio.domain.portfolio.controller;

import com.example.experfolio.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 포트폴리오 관리 컨트롤러
 * Portfolio.txt Use Case 기반으로 구성
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
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> createPortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Object basicInfoDto // TODO: DTO 생성 필요
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.status(HttpStatus.CREATED).body("Portfolio created");
    }

    /**
     * 2.1 포트폴리오 전체 조회
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "내 포트폴리오 조회", description = "본인의 포트폴리오 전체 정보를 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> getMyPortfolio(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.ok("My portfolio");
    }

    /**
     * 2.2 BasicInfo 수정
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "기본정보 수정", description = "포트폴리오의 기본 정보를 수정합니다.")
    @PutMapping("/basic-info")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> updateBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Object basicInfoDto // TODO: DTO 생성 필요
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.ok("BasicInfo updated");
    }

    /**
     * 3.1 포트폴리오 아이템 추가 (최대 5개)
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 추가", description = "프로젝트/활동/연구 등 아이템을 추가합니다. (최대 5개)")
    @PostMapping("/items")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> addPortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "item") Object portfolioItemDto, // TODO: DTO 생성 필요
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.status(HttpStatus.CREATED).body("Item added");
    }

    /**
     * 3.2 포트폴리오 아이템 수정
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 수정", description = "기존 포트폴리오 아이템을 수정합니다.")
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> updatePortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemId,
            @RequestPart(value = "item") Object portfolioItemDto, // TODO: DTO 생성 필요
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.ok("Item updated");
    }

    /**
     * 3.3 포트폴리오 아이템 삭제
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 삭제", description = "특정 포트폴리오 아이템을 삭제합니다.")
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> deletePortfolioItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemId
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.noContent().build();
    }

    /**
     * 3.4 포트폴리오 아이템 순서 변경
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "아이템 순서 변경", description = "포트폴리오 아이템의 순서를 재배치합니다.")
    @PutMapping("/items/reorder")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> reorderPortfolioItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<String> itemIds
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.ok("Items reordered");
    }

    /**
     * 6.1 포트폴리오 전체 삭제
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 완전히 삭제합니다.")
    @DeleteMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> deletePortfolio(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.noContent().build();
    }

    /**
     * 수동 임베딩 트리거 (개발/테스트용)
     * Actor: JOB_SEEKER
     */
    @Operation(summary = "수동 임베딩 트리거", description = "포트폴리오의 임베딩을 수동으로 트리거합니다.")
    @PostMapping("/trigger-embedding")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<?> triggerEmbedding(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // TODO: Python AI 서버 연동
        return ResponseEntity.ok("Embedding triggered");
    }
}