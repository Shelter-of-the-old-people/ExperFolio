package com.example.experfolio.domain.portfolio.dto;

import com.example.experfolio.domain.portfolio.document.Award;
import com.example.experfolio.domain.portfolio.document.Certification;
import com.example.experfolio.domain.portfolio.document.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoDto {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "학교명은 필수입니다")
    private String schoolName;

    @NotBlank(message = "전공은 필수입니다")
    private String major;

    private Double gpa;
    private String desiredPosition;
    private List<String> referenceUrl;
    private List<Award> awards;
    private List<Certification> certifications;
    private List<Language> languages;
}
