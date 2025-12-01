package com.example.experfolio.domain.portfolio.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attachment document for portfolio items
 * Stores file metadata for files uploaded to Cloudflare R2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private String objectKey;           // R2 object key (storage path)
    private String originalFilename;    // Original filename
    private String contentType;         // MIME type
    private Long fileSize;              // File size in bytes
    private String extractionStatus;
}
