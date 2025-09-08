package com.example.experfolio.domain.user.dto;

import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 로그인 응답 DTO
 * 로그인 성공 시 반환되는 사용자 정보와 토큰 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 응답 정보")
public class LoginResponseDto {

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레시 토큰")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "액세스 토큰 만료 시간")
    private LocalDateTime accessTokenExpiresAt;

    @Schema(description = "리프레시 토큰 만료 시간")
    private LocalDateTime refreshTokenExpiresAt;

    @Schema(description = "사용자 정보")
    private UserInfoDto userInfo;

    /**
     * 사용자 기본 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 기본 정보")
    public static class UserInfoDto {

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

        @Schema(description = "계정 상태")
        private UserStatus status;

        @Schema(description = "이메일 인증 여부")
        private boolean emailVerified;

        @Schema(description = "가입일")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 로그인 시간")
        private LocalDateTime lastLoginAt;
    }
}