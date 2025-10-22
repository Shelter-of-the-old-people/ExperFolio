package com.example.experfolio.domain.user.dto;

import com.example.experfolio.domain.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 정보 응답 DTO
 * 사용자 정보 조회 시 반환되는 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserInfoResponseDto {

    @Schema(description = "사용자 ID")
    private UUID userId;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "전화번호")
    private String phoneNumber;

    @Schema(description = "사용자 역할")
    private UserRole role;

    @Schema(description = "가입일")
    private LocalDateTime createdAt;

    @Schema(description = "최근 수정일")
    private LocalDateTime updatedAt;

    @Schema(description = "마지막 로그인 시간")
    private LocalDateTime lastLoginAt;

    @Schema(description = "프로필 완성도 (%)")
    private Integer profileCompletionRate;
}