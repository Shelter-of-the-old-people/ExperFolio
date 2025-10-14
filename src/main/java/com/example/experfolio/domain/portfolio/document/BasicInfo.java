package com.example.experfolio.domain.portfolio.document;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfo {
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