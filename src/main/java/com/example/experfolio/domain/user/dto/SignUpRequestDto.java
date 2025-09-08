package com.example.experfolio.domain.user.dto;

import com.example.experfolio.domain.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 * 사용자 가입 시 필요한 정보를 받는 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청 정보")
public class SignUpRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    @Schema(description = "비밀번호 확인", example = "password123!")
    private String confirmPassword;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @NotNull(message = "사용자 역할은 필수입니다.")
    @Schema(description = "사용자 역할 (JOB_SEEKER: 구직자, RECRUITER: 채용담당자)", 
            allowableValues = {"JOB_SEEKER", "RECRUITER"})
    private UserRole role;

    /**
     * 비밀번호와 비밀번호 확인이 일치하는지 검증
     * @return 일치 여부
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}