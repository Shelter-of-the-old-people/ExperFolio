package com.example.experfolio.domain.favorite.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 즐겨찾기 응답에 포함될 구직자 정보 DTO
 */
@Getter
@Builder
public class JobSeekerInfoDto {
    private String id;
    private String name;
    private String desiredPosition;
    private String major;
    private String schoolName;
    private Double gpa;
}
