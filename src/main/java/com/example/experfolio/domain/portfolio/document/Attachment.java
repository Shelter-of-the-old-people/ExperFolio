package com.example.experfolio.domain.portfolio.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private String filePath;
    private String extractionStatus; // "pending", "completed", "failed"
}