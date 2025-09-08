package com.example.experfolio.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 수정 요청 DTO
 * 사용자가 자신의 기본 정보를 수정할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청")
public class UpdateUserRequestDto {

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일 (변경 시 재인증 필요)", example = "newemail@example.com")
    private String email;

    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
}