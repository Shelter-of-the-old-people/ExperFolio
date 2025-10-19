package com.example.experfolio.domain.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 파일 저장 서비스
 * MultipartFile을 MongoDB 서버 디스크에 직접 저장하고, 경로를 반환
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt"
    );

    /**
     * 단일 파일 저장
     */
    public String saveFile(MultipartFile file, String userId) throws IOException {
        validateFile(file);

        // 업로드 디렉토리 생성
        Path uploadPath = createUploadDirectory(userId);

        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(extension);

        // 파일 저장
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved successfully: {}", targetPath);

        // 저장된 파일의 상대 경로 반환
        return uploadPath.relativize(targetPath).toString();
    }

    /**
     * 다중 파일 저장
     */
    public List<String> saveFiles(MultipartFile[] files, String userId) throws IOException {
        List<String> filePaths = new ArrayList<>();

        if (files == null) {
            return filePaths;
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filePath = saveFile(file, userId);
                filePaths.add(filePath);
            }
        }

        return filePaths;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new RuntimeException("파일 삭제에 실패했습니다", e);
        }
    }

    /**
     * 다중 파일 삭제
     */
    public void deleteFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }

        for (String filePath : filePaths) {
            deleteFile(filePath);
        }
    }

    /**
     * 업로드 디렉토리 생성
     * 구조: /var/data/experfolio/uploads/{userId}/{yyyy-MM-dd}/
     */
    private Path createUploadDirectory(String userId) throws IOException {
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path uploadPath = Paths.get(uploadDir, userId, dateFolder);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadPath);
        }

        return uploadPath;
    }

    /**
     * 고유한 파일명 생성
     * 형식: {UUID}_{timestamp}.{extension}
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", uuid, timestamp, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다");
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("지원하지 않는 파일 형식입니다. 지원 형식: %s", String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        log.info("File validation passed: {} ({}bytes)", originalFilename, file.getSize());
    }

    /**
     * 파일 전체 경로 반환
     */
    public String getFullPath(String relativePath) {
        return Paths.get(uploadDir, relativePath).toString();
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String filePath) {
        Path path = Paths.get(uploadDir, filePath);
        return Files.exists(path);
    }
}
