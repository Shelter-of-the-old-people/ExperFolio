package com.example.experfolio.domain.favorite.controller;

import com.example.experfolio.domain.favorite.dto.FavoriteExistsDto;
import com.example.experfolio.domain.favorite.dto.FavoriteRequestDto;
import com.example.experfolio.domain.favorite.dto.FavoriteResponseDto;
import com.example.experfolio.domain.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 즐겨찾기 API 컨트롤러 (MVP)
 * 리크루터 전용 기능
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite", description = "즐겨찾기 API (리크루터 전용)")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 1. 즐겨찾기 추가
     */
    @Operation(summary = "즐겨찾기 추가", description = "구직자를 즐겨찾기에 추가합니다.")
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<FavoriteResponseDto> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FavoriteRequestDto request
    ) {
        String recruiterId = userDetails.getUsername(); // UUID 문자열
        FavoriteResponseDto response = favoriteService.addFavorite(recruiterId, request.getJobSeekerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 즐겨찾기 제거
     */
    @Operation(summary = "즐겨찾기 제거", description = "즐겨찾기에서 구직자를 제거합니다.")
    @DeleteMapping("/{jobSeekerId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String jobSeekerId
    ) {
        String recruiterId = userDetails.getUsername(); // UUID 문자열
        favoriteService.removeFavorite(recruiterId, jobSeekerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 3. 즐겨찾기 목록 조회 (페이징)
     */
    @Operation(summary = "즐겨찾기 목록 조회", description = "내가 즐겨찾기한 구직자 목록을 조회합니다. (페이징)")
    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<FavoriteResponseDto>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String recruiterId = userDetails.getUsername(); // UUID 문자열
        Page<FavoriteResponseDto> response = favoriteService.getFavorites(recruiterId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 즐겨찾기 여부 확인
     */
    @Operation(summary = "즐겨찾기 여부 확인", description = "특정 구직자가 즐겨찾기되어 있는지 확인합니다.")
    @GetMapping("/{jobSeekerId}/exists")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<FavoriteExistsDto> checkFavoriteExists(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String jobSeekerId
    ) {
        String recruiterId = userDetails.getUsername(); // UUID 문자열
        FavoriteExistsDto response = favoriteService.checkFavoriteExists(recruiterId, jobSeekerId);
        return ResponseEntity.ok(response);
    }
}
