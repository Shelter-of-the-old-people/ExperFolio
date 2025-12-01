package com.example.experfolio.domain.favorite.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 존재 여부 응답 DTO
 */
@Getter
@Builder
public class FavoriteExistsDto {
    private Boolean exists;
    private String favoriteId;  // exists가 true일 때만 포함
    private LocalDateTime createdAt;  // exists가 true일 때만 포함
}
