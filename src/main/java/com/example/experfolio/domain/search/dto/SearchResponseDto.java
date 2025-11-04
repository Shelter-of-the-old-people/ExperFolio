package com.example.experfolio.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 검색 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 응답")
public class SearchResponseDto {

    @JsonProperty("status")
    @Schema(description = "응답 상태", example = "success")
    private String status;

    @JsonProperty("candidates")
    @Schema(description = "후보자 목록")
    private List<CandidateDto> candidates;

    @JsonProperty("searchTime")
    @Schema(description = "검색 소요 시간", example = "8.17s")
    private String searchTime;

    @JsonProperty("totalResults")
    @Schema(description = "전체 결과 수", example = "10")
    private Integer totalResults;
}
