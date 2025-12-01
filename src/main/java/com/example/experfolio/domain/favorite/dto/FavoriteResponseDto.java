package com.example.experfolio.domain.favorite.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 응답 DTO
 */
@Getter
@Builder
public class FavoriteResponseDto {
    private String id;
    private JobSeekerInfoDto jobSeeker;
    private LocalDateTime createdAt;
}
