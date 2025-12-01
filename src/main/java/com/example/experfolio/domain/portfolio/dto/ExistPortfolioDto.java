package com.example.experfolio.domain.portfolio.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExistPortfolioDto {
    private String userId;
    private boolean isExist;
}
