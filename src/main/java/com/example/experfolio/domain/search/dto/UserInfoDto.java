package com.example.experfolio.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 검색 결과에 포함될 사용자 기본 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    /**
     * 이름
     */
    @JsonProperty("name")
    private String name;

    /**
     * 학교명
     */
    @JsonProperty("schoolName")
    private String schoolName;

    /**
     * 학점 (GPA)
     */
    @JsonProperty("gpa")
    private Double gpa;

    /**
     * 전공
     */
    @JsonProperty("major")
    private String major;

    /**
     * 수상 경력 개수
     */
    @JsonProperty("awardsCount")
    private Integer awardsCount;
}
