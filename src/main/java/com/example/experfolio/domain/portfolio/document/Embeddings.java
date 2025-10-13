package com.example.experfolio.domain.portfolio.document;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Embeddings {
    private String searchableText;
    private List<Double> kureVector;
    private LocalDateTime lastUpdated;
}
