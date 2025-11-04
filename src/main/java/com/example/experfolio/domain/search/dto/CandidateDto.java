package com.example.experfolio.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 후보자 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "후보자 정보")
public class CandidateDto {

    @JsonProperty("userId")
    @Schema(description = "사용자 ID", example = "ehdgus-100")
    private String userId;

    @JsonProperty("matchScore")
    @Schema(description = "매칭 점수 (0.0 ~ 1.0)", example = "0.9")
    private Double matchScore;

    @JsonProperty("matchReason")
    @Schema(description = "매칭 이유", example = "React와 TypeScript 경험은 포트폴리오의 'React와 TypeScript를 이용한 영화 검색 사이트 제작' 프로젝트에서 확인됩니다.")
    private String matchReason;

    @JsonProperty("keywords")
    @Schema(description = "키워드 목록", example = "[\"React\", \"TypeScript\", \"영화 검색 사이트\"]")
    private List<String> keywords;
}
