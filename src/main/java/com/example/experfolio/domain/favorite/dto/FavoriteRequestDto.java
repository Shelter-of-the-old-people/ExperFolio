package com.example.experfolio.domain.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 즐겨찾기 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequestDto {

    @NotBlank(message = "구직자 ID는 필수입니다")
    private String jobSeekerId;
}
