# FileSystemUpdate.md - Cloudflare R2 Migration Plan

## 1. Current Structure Analysis

### Current File Storage Method
```
Local File System: uploads/{userId}/{yyyy-MM-dd}/{uuid_timestamp.ext}
```

### Related Files
| File | Role |
|------|------|
| `FileStorageService.java` | File save/delete/path management |
| `Attachment.java` | MongoDB embedded document (filePath, extractionStatus) |
| `PortfolioService.java` | Calls FileStorageService |
| `PortfolioController.java` | Receives MultipartFile |

### Current FileStorageService Methods
```java
public String saveFile(MultipartFile file, String userId);  // Single file save
public List<String> saveFiles(MultipartFile[] files, String userId);  // Multiple files save
public void deleteFile(String filePath);                   // Single file delete
public void deleteFiles(List<String> filePaths);               // Multiple files delete
public String getFullPath(String relativePath);                // Return full path
public boolean fileExists(String filePath);                    // Check file existence
```

---

## 2. Cloudflare R2 Overview

### R2 Features
- S3 Compatible API (Can use AWS SDK)
- No Egress cost (Free data transfer)
- 10GB Free tier
- CDN Integration available (Cloudflare Workers)

### R2 Connection Info (Required Environment Variables)
```env
R2_ACCOUNT_ID=<cloudflare-account-id>
R2_ACCESS_KEY_ID=<access-key>
R2_SECRET_ACCESS_KEY=<secret-key>
R2_BUCKET_NAME=experfolio-files
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_PUBLIC_URL=https://files.experfolio.com  # or R2 public URL
```

---

## 3. Refactoring Plan

### 3.1 Add Dependencies (build.gradle)

```gradle
dependencies {
    // AWS S3 SDK (R2 Compatible)
    implementation 'software.amazon.awssdk:s3:2.21.0'
    implementation 'software.amazon.awssdk:auth:2.21.0'
}
```

### 3.2 Add Configuration Class

**File**: `src/main/java/com/example/experfolio/global/config/R2Config.java`

```java
package com.example.experfolio.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${r2.access-key-id}")
    private String accessKeyId;

    @Value("${r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${r2.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))  // R2 uses auto region
                .build();
    }
}
```

### 3.3 FileStorageService Refactoring

**File**: `src/main/java/com/example/experfolio/domain/portfolio/service/FileStorageService.java`

```java
package com.example.experfolio.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt"
    );

    /**
     * Save single file (R2 Upload)
     * @return R2 object key (storage path)
     */
    public String saveFile(MultipartFile file, String userId) throws IOException {
        validateFile(file);

        // Generate unique file key: {userId}/{date}/{uuid_timestamp.ext}
        String objectKey = generateObjectKey(userId, file.getOriginalFilename());

        // Set Content-Type
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // R2 Upload
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("File uploaded to R2: {}", objectKey);

        return objectKey;
    }

    /**
     * Save multiple files
     */
    public List<String> saveFiles(MultipartFile[] files, String userId) throws IOException {
        List<String> objectKeys = new ArrayList<>();

        if (files == null) {
            return objectKeys;
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String objectKey = saveFile(file, userId);
                objectKeys.add(objectKey);
            }
        }

        return objectKeys;
    }

    /**
     * Delete file (Delete from R2)
     */
    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from R2: {}", objectKey);
        } catch (S3Exception e) {
            log.error("Failed to delete file from R2: {}", objectKey, e);
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        // Batch delete (more efficient)
        List<ObjectIdentifier> toDelete = objectKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(toDelete).build())
                .build();

        try {
            s3Client.deleteObjects(deleteRequest);
            log.info("Files deleted from R2: {} files", objectKeys.size());
        } catch (S3Exception e) {
            log.error("Failed to delete files from R2", e);
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * Return public URL for file
     */
    public String getPublicUrl(String objectKey) {
        return publicUrl + "/" + objectKey;
    }

    /**
     * Check file existence
     */
    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Generate Presigned URL (temporary access)
     * Use when temporary access to private files is needed
     */
    // public String generatePresignedUrl(String objectKey, Duration expiration) {
    //     // Requires S3Presigner - implement if needed
    // }

    /**
     * Generate Object Key
     * Format: portfolios/{userId}/{yyyy-MM-dd}/{uuid}_{timestamp}.{extension}
     */
    private String generateObjectKey(String userId, String originalFilename) {
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("portfolios/%s/%s/%s_%s.%s", userId, dateFolder, uuid, timestamp, extension);
    }

    /**
     * Extract file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size too large. Max size: %dMB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("Unsupported file format. Supported formats: %s", String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        log.info("File validation passed: {} ({}bytes)", originalFilename, file.getSize());
    }
}
```

### 3.4 Attachment Document Modification

**File**: `src/main/java/com/example/experfolio/domain/portfolio/document/Attachment.java`

```java
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
    private String objectKey;           // R2 object key (replaces filePath)
    private String originalFilename;    // Original filename (new)
    private String contentType;         // MIME type (new)
    private Long fileSize;              // File size (new)
    private String extractionStatus;    // OCR extraction status (keep)

    // Deprecated - remove after migration
    @Deprecated
    private String filePath;
}
```

### 3.5 PortfolioService Modifications

Changes needed:

```java
// Before: Save filePath
Attachment attachment = Attachment.builder()
        .filePath(filePath)
        .extractionStatus("pending")
        .build();

// After: Save objectKey + additional metadata
Attachment attachment = Attachment.builder()
        .objectKey(objectKey)
        .originalFilename(file.getOriginalFilename())
        .contentType(file.getContentType())
        .fileSize(file.getSize())
        .extractionStatus("pending")
        .build();
```

### 3.6 Add File Download Endpoint (Optional)

**File**: Add to `PortfolioController.java`

```java
/**
 * Get attachment download URL
 */
@Operation(summary = "Get attachment URL", description = "Returns public URL for attachment.")
@GetMapping("/files/{objectKey}")
public ResponseEntity<Map<String, String>> getFileUrl(
        @PathVariable String objectKey
) {
    String url = fileStorageService.getPublicUrl(objectKey);
    return ResponseEntity.ok(Map.of("url", url));
}
```

---

## 4. Environment Variable Configuration

### 4.1 Add to application.yml

```yaml
# Cloudflare R2 Configuration
r2:
  account-id: ${R2_ACCOUNT_ID}
  access-key-id: ${R2_ACCESS_KEY_ID}
  secret-access-key: ${R2_SECRET_ACCESS_KEY}
  bucket-name: ${R2_BUCKET_NAME:experfolio-files}
  endpoint: https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
  public-url: ${R2_PUBLIC_URL:https://pub-xxx.r2.dev}
```

### 4.2 Add to .env file

```env
# Cloudflare R2
R2_ACCOUNT_ID=your-account-id
R2_ACCESS_KEY_ID=your-access-key
R2_SECRET_ACCESS_KEY=your-secret-key
R2_BUCKET_NAME=experfolio-files
R2_PUBLIC_URL=https://pub-xxx.r2.dev
```

---

## 5. Cloudflare R2 Bucket Setup

### 5.1 Create Bucket
1. Cloudflare Dashboard > R2 > Create bucket
2. Bucket name: `experfolio-files`
3. Location: Automatic (or nearest region)

### 5.2 Create API Token
1. R2 > Manage R2 API Tokens
2. Create API Token
3. Permissions: Object Read & Write
4. Specify bucket: `experfolio-files`
5. Save Access Key ID / Secret Access Key

### 5.3 Public Access Setup (Optional)
- **Option A**: R2 Public Bucket (Simple)
  - Settings > Public access > Allow Access

- **Option B**: Custom Domain Connection
  - Settings > Custom Domains > Connect Domain
  - Connect subdomain like `files.experfolio.com`

### 5.4 CORS Configuration
```json
[
  {
    "AllowedOrigins": ["https://experfolio.com", "http://localhost:3000"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedHeaders": ["*"],
    "MaxAgeSeconds": 3600
  }
]
```

---

## 6. Migration Steps

### Phase 1: Preparation (Day 1)
- [ ] Add build.gradle dependencies
- [ ] Create R2Config.java
- [ ] Add R2 config to application.yml
- [ ] Add R2 credentials to .env file
- [ ] Create and configure Cloudflare R2 bucket

### Phase 2: Code Modification (Day 2)
- [ ] Full refactoring of FileStorageService.java
- [ ] Add/modify fields in Attachment.java
- [ ] Modify file storage logic in PortfolioService.java

### Phase 3: Testing (Day 3)
- [ ] Write unit tests (FileStorageService)
- [ ] Integration tests (file upload/download)
- [ ] Verify existing API functionality

### Phase 4: Data Migration (Day 4)
- [ ] Write script to upload existing local files to R2
- [ ] Convert MongoDB filePath to objectKey
- [ ] Verify migration

### Phase 5: Deployment (Day 5)
- [ ] Staging environment testing
- [ ] Production deployment
- [ ] Backup and cleanup local uploads folder

---

## 7. Changed Files Summary

| Action | File Path |
|--------|-----------|
| New | `global/config/R2Config.java` |
| Full Modify | `portfolio/service/FileStorageService.java` |
| Add Fields | `portfolio/document/Attachment.java` |
| Partial Modify | `portfolio/service/PortfolioService.java` |
| Partial Modify | `portfolio/controller/PortfolioController.java` |
| Add Config | `application.yml` |
| Add Dependency | `build.gradle` |

---

## 8. Rollback Plan

In case of issues:

1. **Code Rollback**: Restore previous version with Git revert
2. **Data Restore**: Restore from local uploads folder backup
3. **Environment Variables**: Remove R2 related environment variables

---

## 9. Expected Cost

### Cloudflare R2 Pricing (2024)
- Storage: $0.015/GB/month (10GB Free)
- Class A operations (PUT, POST): $4.50/million requests
- Class B operations (GET): $0.36/million requests
- Egress: **Free**

### Expected Monthly Cost (Initial)
- File storage: 5GB = Free
- Upload: 1000 requests = ~$0.005
- Download: 10000 requests = ~$0.004
- **Total Expected**: Almost free (within free tier)

---

## 10. Additional Considerations

### 10.1 Image Optimization (Future)
- Auto resizing with Cloudflare Images integration
- Size reduction with WebP conversion

### 10.2 CDN Caching
- R2 + Cloudflare CDN automatic integration
- Cache-Control header configuration

### 10.3 Security
- Temporary access with Presigned URL
- Access control with bucket policy
- Apply Rate limiting

---

**Created**: 2025-11-21
**Author**: Claude Code
**Status**: Planning Stage
