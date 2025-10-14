package com.example.experfolio.domain.portfolio.dto;

import com.example.experfolio.domain.portfolio.document.BasicInfo;
import com.example.experfolio.domain.portfolio.document.PortfolioItem;
import com.example.experfolio.domain.portfolio.document.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDto {

    private String portfolioId;
    private String userId;
    private BasicInfo basicInfo;
    private List<PortfolioItem> portfolioItems;
    private Integer portfolioItemCount;
    private ProcessingStatus processingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
