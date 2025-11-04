package com.example.experfolio.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 검색 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 요청")
public class SearchRequestDto {

    @NotBlank(message = "검색 쿼리는 필수입니다")
    @Size(max = 500, message = "검색 쿼리는 최대 500자까지 입력 가능합니다")
    @Schema(description = "검색 쿼리", example = "React와 TypeScript 경험이 있는 프론트엔드 개발자", required = true)
    private String query;
}
