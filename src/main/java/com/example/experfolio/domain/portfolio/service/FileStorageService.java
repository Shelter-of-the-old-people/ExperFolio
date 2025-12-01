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

/**
 * File Storage Service using Cloudflare R2
 * Uploads files to R2 bucket and returns object keys
 */
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
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt", "hwp"
    );

    /**
     * Save single file to R2
     * @return R2 object key (storage path)
     */
    public String saveFile(MultipartFile file, String userId) throws IOException {
        validateFile(file);

        String objectKey = generateObjectKey(userId, file.getOriginalFilename());

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

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
     * Save multiple files to R2
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
     * Delete file from R2
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
     * Delete multiple files from R2 (batch delete)
     */
    public void deleteFiles(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

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
     * Get public URL for file
     */
    public String getPublicUrl(String objectKey) {
        return publicUrl + "/" + objectKey;
    }

    /**
     * Check if file exists in R2
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
     * Generate unique object key
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
