package com.example.experfolio.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 * 사용자가 비밀번호를 변경할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청")
public class ChangePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호", example = "currentPassword123!")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8~20자 사이여야 합니다.")
    @Schema(description = "새 비밀번호", example = "newPassword123!")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    @Schema(description = "새 비밀번호 확인", example = "newPassword123!")
    private String confirmNewPassword;

    /**
     * 새 비밀번호와 비밀번호 확인이 일치하는지 검증
     * @return 일치 여부
     */
    public boolean isNewPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }
}