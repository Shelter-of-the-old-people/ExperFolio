package com.example.experfolio.domain.portfolio.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfolios")
public class Portfolio {

    @Id
    private String id;

    @Field("userId")
    private String userId;

    @Field("basicInfo")
    private BasicInfo basicInfo;

    @Field("portfolioItems")
    private List<PortfolioItem> portfolioItems;

    @Field("embeddings")
    private Embeddings embeddings;

    @Field("processingStatus")
    private ProcessingStatus processingStatus;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;
}

