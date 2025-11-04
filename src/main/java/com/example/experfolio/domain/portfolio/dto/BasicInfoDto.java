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

    private String name;
    private String schoolName;
    private String major;

    private Double gpa;
    private String desiredPosition;
    private List<String> referenceUrl;
    private List<Award> awards;
    private List<Certification> certifications;
    private List<Language> languages;
}
