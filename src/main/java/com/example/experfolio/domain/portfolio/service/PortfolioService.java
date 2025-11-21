package com.example.experfolio.domain.portfolio.service;

import com.example.experfolio.domain.portfolio.document.*;
import com.example.experfolio.domain.portfolio.dto.BasicInfoDto;
import com.example.experfolio.domain.portfolio.dto.ExistPortfolioDto;
import com.example.experfolio.domain.portfolio.dto.PortfolioItemDto;
import com.example.experfolio.domain.portfolio.dto.PortfolioResponseDto;
import com.example.experfolio.domain.portfolio.repository.PortfolioRepository;
import com.example.experfolio.domain.user.entity.JobSeekerProfile;
import com.example.experfolio.domain.user.repository.JobSeekerProfileRepository;
import com.example.experfolio.domain.user.service.JobSeekerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 포트폴리오 서비스
 * Portfolio.txt Use Case 기반 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final FileStorageService fileStorageService;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobSeekerProfileService jobSeekerProfileService;

    private static final int MAX_PORTFOLIO_ITEMS = 5;

    /**
     * 1.1 포트폴리오 생성
     */
    @Transactional
    public PortfolioResponseDto createPortfolio(String userId, BasicInfoDto basicInfoDto) {
        log.info("Creating portfolio for userId: {}", userId);

        // 중복 포트폴리오 체크
        if (portfolioRepository.existsByUserId(userId)) {
            throw new IllegalStateException("포트폴리오가 이미 존재합니다");
        }

       jobSeekerProfileService.createProfile(UUID.fromString(userId));

        // BasicInfo 생성
        BasicInfo basicInfo = BasicInfo.builder()
                .name(basicInfoDto.getName())
                .schoolName(basicInfoDto.getSchoolName())
                .major(basicInfoDto.getMajor())
                .gpa(basicInfoDto.getGpa())
                .desiredPosition(basicInfoDto.getDesiredPosition())
                .referenceUrl(basicInfoDto.getReferenceUrl())
                .awards(basicInfoDto.getAwards())
                .certifications(basicInfoDto.getCertifications())
                .languages(basicInfoDto.getLanguages())
                .build();

        // ProcessingStatus 초기화
        ProcessingStatus processingStatus = ProcessingStatus.builder()
                .needsEmbedding(true)
                .lastProcessed(null)
                .build();

        // Portfolio Document 생성
        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .basicInfo(basicInfo)
                .portfolioItems(new ArrayList<>())
                .embeddings(null)
                .processingStatus(processingStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio created with id: {}", savedPortfolio.getId());

        // PostgreSQL JobSeekerProfile에 portfolioId 저장
        try {
            JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findByUserId(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("구직자 프로필을 찾을 수 없습니다"));

            jobSeekerProfile.setPortfolioId(savedPortfolio.getId());
            jobSeekerProfileRepository.save(jobSeekerProfile);
            log.info("PortfolioId saved to JobSeekerProfile for userId: {}", userId);
        } catch (IllegalArgumentException e) {
            // 포트폴리오는 생성되었지만 JobSeekerProfile이 없는 경우
            log.error("JobSeekerProfile not found for userId: {}", userId, e);
            // MongoDB 포트폴리오 롤백
            portfolioRepository.delete(savedPortfolio);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update JobSeekerProfile with portfolioId for userId: {}", userId, e);
            // MongoDB 포트폴리오 롤백
            portfolioRepository.delete(savedPortfolio);
            throw new RuntimeException("포트폴리오 생성 중 오류가 발생했습니다", e);
        }

        return convertToResponseDto(savedPortfolio);
    }

    /**
     * 1.2 BasicInfo 전체 조회
     */
    @Transactional(readOnly = true)
    public PortfolioResponseDto getMyBasicInfo(String userId) {
        log.info("Fetching basicInfo for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        return convertToResponseDto(portfolio);
    }

    /**
     * 2.1 포트폴리오 전체 조회
     */
    @Transactional(readOnly = true)
    public PortfolioResponseDto getMyPortfolio(String userId) {
        log.info("Fetching portfolio for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // portfolioItems를 order 순으로 정렬
        if (portfolio.getPortfolioItems() != null) {
            portfolio.getPortfolioItems().sort(Comparator.comparingInt(PortfolioItem::getOrder));
        }

        return convertToResponseDto(portfolio);
    }

    /**
     * 2.2 BasicInfo 수정
     */
    @Transactional
    public PortfolioResponseDto updateBasicInfo(String userId, BasicInfoDto basicInfoDto) {
        log.info("Updating basicInfo for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // BasicInfo 업데이트
        BasicInfo basicInfo = BasicInfo.builder()
                .name(basicInfoDto.getName())
                .schoolName(basicInfoDto.getSchoolName())
                .major(basicInfoDto.getMajor())
                .gpa(basicInfoDto.getGpa())
                .desiredPosition(basicInfoDto.getDesiredPosition())
                .referenceUrl(basicInfoDto.getReferenceUrl())
                .awards(basicInfoDto.getAwards())
                .certifications(basicInfoDto.getCertifications())
                .languages(basicInfoDto.getLanguages())
                .build();

        portfolio.setBasicInfo(basicInfo);
        portfolio.setUpdatedAt(LocalDateTime.now());

        // 재임베딩 플래그 설정
        portfolio.getProcessingStatus().setNeedsEmbedding(true);

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("BasicInfo updated for portfolioId: {}", updatedPortfolio.getId());

        return convertToResponseDto(updatedPortfolio);
    }

    /**
     * 3.1 포트폴리오 아이템 추가
     */
    @Transactional
    public PortfolioResponseDto addPortfolioItem(String userId, PortfolioItemDto itemDto, MultipartFile[] files) {
        log.info("Adding portfolio item for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // 최대 개수 체크
        if (portfolio.getPortfolioItems().size() >= MAX_PORTFOLIO_ITEMS) {
            throw new IllegalStateException("포트폴리오 아이템은 최대 5개까지 추가 가능합니다");
        }

        // 파일 업로드 처리 (R2)
        List<Attachment> attachments = new ArrayList<>();
        if (files != null && files.length > 0) {
            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String objectKey = fileStorageService.saveFile(file, userId);
                        Attachment attachment = Attachment.builder()
                                .objectKey(objectKey)
                                .originalFilename(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .fileSize(file.getSize())
                                .extractionStatus("pending")
                                .build();
                        attachments.add(attachment);
                    }
                }
            } catch (Exception e) {
                log.error("File upload failed for userId: {}", userId, e);
                throw new RuntimeException("파일 업로드에 실패했습니다", e);
            }
        }

        // 새 order 계산 (기존 최대값 + 1)
        int newOrder = portfolio.getPortfolioItems().stream()
                .mapToInt(PortfolioItem::getOrder)
                .max()
                .orElse(0) + 1;

        // PortfolioItem 생성
        PortfolioItem newItem = PortfolioItem.builder()
                .id(UUID.randomUUID().toString())
                .order(newOrder)
                .type(itemDto.getType())
                .title(itemDto.getTitle())
                .content(itemDto.getContent())
                .attachments(attachments)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        portfolio.getPortfolioItems().add(newItem);
        portfolio.setUpdatedAt(LocalDateTime.now());

        // 재임베딩 플래그 설정
        portfolio.getProcessingStatus().setNeedsEmbedding(true);

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio item added with id: {}", newItem.getId());

        return convertToResponseDto(updatedPortfolio);
    }

    /**
     * 3.2 포트폴리오 아이템 수정
     */
    @Transactional
    public PortfolioResponseDto updatePortfolioItem(String userId, String itemId, PortfolioItemDto itemDto, MultipartFile[] files) {
        log.info("Updating portfolio item {} for userId: {}", itemId, userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // 아이템 찾기
        PortfolioItem targetItem = portfolio.getPortfolioItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 아이템을 찾을 수 없습니다"));

        // 아이템 업데이트
        targetItem.setType(itemDto.getType());
        targetItem.setTitle(itemDto.getTitle());
        targetItem.setContent(itemDto.getContent());
        targetItem.setUpdatedAt(LocalDateTime.now());

        // 파일 업로드 처리 (기존 파일에 추가, R2)
        if (files != null && files.length > 0) {
            List<Attachment> attachments = targetItem.getAttachments();
            if (attachments == null) {
                attachments = new ArrayList<>();
                targetItem.setAttachments(attachments);
            }

            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String objectKey = fileStorageService.saveFile(file, userId);
                        Attachment attachment = Attachment.builder()
                                .objectKey(objectKey)
                                .originalFilename(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .fileSize(file.getSize())
                                .extractionStatus("pending")
                                .build();
                        attachments.add(attachment);
                    }
                }
            } catch (Exception e) {
                log.error("File upload failed for userId: {}, itemId: {}", userId, itemId, e);
                throw new RuntimeException("파일 업로드에 실패했습니다", e);
            }
        }

        portfolio.setUpdatedAt(LocalDateTime.now());

        // 재임베딩 플래그 설정
        portfolio.getProcessingStatus().setNeedsEmbedding(true);

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio item updated: {}", itemId);

        return convertToResponseDto(updatedPortfolio);
    }

    /**
     * 3.3 포트폴리오 아이템 삭제
     */
    @Transactional
    public void deletePortfolioItem(String userId, String itemId) {
        log.info("Deleting portfolio item {} for userId: {}", itemId, userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // 아이템 찾기
        PortfolioItem targetItem = portfolio.getPortfolioItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 아이템을 찾을 수 없습니다"));

        // 첨부파일 삭제 (R2)
        if (targetItem.getAttachments() != null && !targetItem.getAttachments().isEmpty()) {
            List<String> objectKeys = targetItem.getAttachments().stream()
                    .map(Attachment::getObjectKey)
                    .toList();
            fileStorageService.deleteFiles(objectKeys);
        }

        // 아이템 삭제
        portfolio.getPortfolioItems().remove(targetItem);
        portfolio.setUpdatedAt(LocalDateTime.now());

        // 재임베딩 플래그 설정
        portfolio.getProcessingStatus().setNeedsEmbedding(true);

        portfolioRepository.save(portfolio);
        log.info("Portfolio item deleted: {}", itemId);
    }

    /**
     * 3.4 포트폴리오 아이템 순서 변경
     */
    @Transactional
    public PortfolioResponseDto reorderPortfolioItems(String userId, List<String> itemIds) {
        log.info("Reordering portfolio items for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // itemIds 순서대로 order 업데이트
        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            PortfolioItem item = portfolio.getPortfolioItems().stream()
                    .filter(pi -> pi.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("포트폴리오 아이템을 찾을 수 없습니다: " + itemId));

            item.setOrder(i + 1);
        }

        portfolio.setUpdatedAt(LocalDateTime.now());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio items reordered");

        return convertToResponseDto(updatedPortfolio);
    }

    /**
     * 6.1 포트폴리오 전체 삭제
     */
    @Transactional
    public void deletePortfolio(String userId) {
        log.info("Deleting portfolio for userId: {}", userId);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // 모든 첨부파일 삭제 (R2)
        if (portfolio.getPortfolioItems() != null) {
            for (PortfolioItem item : portfolio.getPortfolioItems()) {
                if (item.getAttachments() != null && !item.getAttachments().isEmpty()) {
                    List<String> objectKeys = item.getAttachments().stream()
                            .map(Attachment::getObjectKey)
                            .toList();
                    fileStorageService.deleteFiles(objectKeys);
                }
            }
        }

        // MongoDB 문서 삭제
        portfolioRepository.delete(portfolio);

        // PostgreSQL JobSeekerProfile의 portfolioId NULL 처리
        try {
            JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findByUserId(UUID.fromString(userId))
                    .orElse(null);

            if (jobSeekerProfile != null) {
                jobSeekerProfile.setPortfolioId(null);
                jobSeekerProfileRepository.save(jobSeekerProfile);
                log.info("PortfolioId cleared from JobSeekerProfile for userId: {}", userId);
            } else {
                log.warn("JobSeekerProfile not found for userId: {} during portfolio deletion", userId);
            }
        } catch (Exception e) {
            // 포트폴리오는 이미 삭제되었으므로 경고만 남기고 계속 진행
            log.warn("Failed to update JobSeekerProfile portfolioId for userId: {}", userId, e);
        }

        log.info("Portfolio deleted for userId: {}", userId);
    }

    /**
     * Portfolio → PortfolioResponseDto 변환
     */
    private PortfolioResponseDto convertToResponseDto(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .portfolioId(portfolio.getId())
                .userId(portfolio.getUserId())
                .basicInfo(portfolio.getBasicInfo())
                .portfolioItems(portfolio.getPortfolioItems())
                .portfolioItemCount(portfolio.getPortfolioItems() != null ? portfolio.getPortfolioItems().size() : 0)
                .processingStatus(portfolio.getProcessingStatus())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    public ExistPortfolioDto getExistPortfolio(String userId) {
        return ExistPortfolioDto.builder()
                .userId(userId)
                .isExist(portfolioRepository.existsByUserId(userId))
                .build();
    }
}
