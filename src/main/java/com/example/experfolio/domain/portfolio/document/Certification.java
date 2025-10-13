package com.example.experfolio.domain.portfolio.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    private String certificationName;
    private String issueY;
}