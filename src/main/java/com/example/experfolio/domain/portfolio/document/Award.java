package com.example.experfolio.domain.portfolio.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/* 수상경력
* */
public class Award {
    private String awardName;
    private String achievement;
    private String awardY;
}