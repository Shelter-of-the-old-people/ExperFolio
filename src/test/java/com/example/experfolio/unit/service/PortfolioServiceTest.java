package com.example.experfolio.unit.service;

import com.example.experfolio.domain.portfolio.document.*;
import com.example.experfolio.domain.portfolio.dto.BasicInfoDto;
import com.example.experfolio.domain.portfolio.dto.PortfolioResponseDto;
import com.example.experfolio.domain.portfolio.repository.PortfolioRepository;
import com.example.experfolio.domain.portfolio.service.FileStorageService;
import com.example.experfolio.domain.portfolio.service.PortfolioService;
import com.example.experfolio.domain.user.entity.JobSeekerProfile;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.repository.JobSeekerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService 단위 테스트")
class PortfolioServiceTest {

    // 테스트용 상수
    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    private static final String TEST_PORTFOLIO_ID = "portfolio-mongo-id-123";
    private static final String TEST_NAME = "홍길동";
    private static final String TEST_SCHOOL = "한국대학교";
    private static final String TEST_MAJOR = "컴퓨터공학";
    private static final Double TEST_GPA = 3.8;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;
    private JobSeekerProfile testJobSeekerProfile;
    private User testUser;
    private BasicInfoDto testBasicInfoDto;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testJobSeekerProfile = createTestJobSeekerProfile();
        testPortfolio = createTestPortfolio();
        testBasicInfoDto = createTestBasicInfoDto();
    }

    @Nested
    @DisplayName("포트폴리오 생성 테스트")
    class CreatePortfolioTests {

        @Test
        @DisplayName("유효한 데이터로 포트폴리오 생성 - JobSeekerProfile 연동 성공")
        void givenValidData_whenCreatePortfolio_thenCreateSuccessfullyAndUpdateJobSeekerProfile() {
            // Given
            given(portfolioRepository.existsByUserId(TEST_USER_ID)).willReturn(false);
            given(portfolioRepository.save(any(Portfolio.class))).willReturn(testPortfolio);
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));
            given(jobSeekerProfileRepository.save(any(JobSeekerProfile.class)))
                    .willReturn(testJobSeekerProfile);

            // When
            PortfolioResponseDto result = portfolioService.createPortfolio(TEST_USER_ID, testBasicInfoDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getBasicInfo()).isNotNull();
            assertThat(result.getBasicInfo().getName()).isEqualTo(TEST_NAME);

            // MongoDB Portfolio 저장 확인
            verify(portfolioRepository).save(any(Portfolio.class));

            // PostgreSQL JobSeekerProfile 조회 및 업데이트 확인
            verify(jobSeekerProfileRepository).findByUserId(TEST_USER_UUID);
            verify(jobSeekerProfileRepository).save(argThat(profile ->
                profile.getPortfolioId() != null
            ));
        }

        @Test
        @DisplayName("중복 포트폴리오 생성 시도 - 실패")
        void givenExistingPortfolio_whenCreatePortfolio_thenThrowException() {
            // Given
            given(portfolioRepository.existsByUserId(TEST_USER_ID)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> portfolioService.createPortfolio(TEST_USER_ID, testBasicInfoDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("포트폴리오가 이미 존재합니다");

            verify(portfolioRepository, never()).save(any());
            verify(jobSeekerProfileRepository, never()).findByUserId(any());
        }

        @Test
        @DisplayName("JobSeekerProfile이 없을 때 포트폴리오 생성 - 실패 및 롤백")
        void givenNoJobSeekerProfile_whenCreatePortfolio_thenRollbackAndThrowException() {
            // Given
            given(portfolioRepository.existsByUserId(TEST_USER_ID)).willReturn(false);
            given(portfolioRepository.save(any(Portfolio.class))).willReturn(testPortfolio);
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioService.createPortfolio(TEST_USER_ID, testBasicInfoDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("구직자 프로필을 찾을 수 없습니다");

            // MongoDB 포트폴리오 롤백 확인
            verify(portfolioRepository).delete(testPortfolio);
            verify(jobSeekerProfileRepository, never()).save(any());
        }

        @Test
        @DisplayName("JobSeekerProfile 업데이트 실패 시 - 롤백")
        void givenJobSeekerProfileUpdateFails_whenCreatePortfolio_thenRollbackAndThrowException() {
            // Given
            given(portfolioRepository.existsByUserId(TEST_USER_ID)).willReturn(false);
            given(portfolioRepository.save(any(Portfolio.class))).willReturn(testPortfolio);
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));
            given(jobSeekerProfileRepository.save(any(JobSeekerProfile.class)))
                    .willThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> portfolioService.createPortfolio(TEST_USER_ID, testBasicInfoDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("포트폴리오 생성 중 오류가 발생했습니다");

            // MongoDB 포트폴리오 롤백 확인
            verify(portfolioRepository).delete(testPortfolio);
        }

        @Test
        @DisplayName("ProcessingStatus 초기화 확인")
        void whenCreatePortfolio_thenProcessingStatusInitialized() {
            // Given
            given(portfolioRepository.existsByUserId(TEST_USER_ID)).willReturn(false);
            given(portfolioRepository.save(any(Portfolio.class))).willReturn(testPortfolio);
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));
            given(jobSeekerProfileRepository.save(any(JobSeekerProfile.class)))
                    .willReturn(testJobSeekerProfile);

            // When
            PortfolioResponseDto result = portfolioService.createPortfolio(TEST_USER_ID, testBasicInfoDto);

            // Then
            assertThat(result.getProcessingStatus()).isNotNull();
            assertThat(result.getProcessingStatus().isNeedsEmbedding()).isTrue();

            verify(portfolioRepository).save(argThat(portfolio ->
                portfolio.getProcessingStatus() != null &&
                portfolio.getProcessingStatus().isNeedsEmbedding()
            ));
        }
    }

    @Nested
    @DisplayName("포트폴리오 삭제 테스트")
    class DeletePortfolioTests {

        @Test
        @DisplayName("포트폴리오 삭제 - JobSeekerProfile portfolioId NULL 처리 성공")
        void givenExistingPortfolio_whenDeletePortfolio_thenDeleteAndClearJobSeekerProfilePortfolioId() {
            // Given
            testPortfolio.setPortfolioItems(new ArrayList<>());
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(testPortfolio));
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));
            given(jobSeekerProfileRepository.save(any(JobSeekerProfile.class)))
                    .willReturn(testJobSeekerProfile);

            // When
            portfolioService.deletePortfolio(TEST_USER_ID);

            // Then
            // MongoDB 포트폴리오 삭제 확인
            verify(portfolioRepository).delete(testPortfolio);

            // PostgreSQL JobSeekerProfile의 portfolioId NULL 처리 확인
            verify(jobSeekerProfileRepository).findByUserId(TEST_USER_UUID);
            verify(jobSeekerProfileRepository).save(argThat(profile ->
                profile.getPortfolioId() == null
            ));
        }

        @Test
        @DisplayName("포트폴리오 삭제 시 JobSeekerProfile이 없어도 - 정상 완료")
        void givenNoJobSeekerProfile_whenDeletePortfolio_thenCompleteSuccessfully() {
            // Given
            testPortfolio.setPortfolioItems(new ArrayList<>());
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(testPortfolio));
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.empty());

            // When
            portfolioService.deletePortfolio(TEST_USER_ID);

            // Then
            // MongoDB 포트폴리오는 삭제되어야 함
            verify(portfolioRepository).delete(testPortfolio);

            // JobSeekerProfile 저장은 시도하지 않음
            verify(jobSeekerProfileRepository, never()).save(any());
        }

        @Test
        @DisplayName("포트폴리오 삭제 시 JobSeekerProfile 업데이트 실패해도 - 정상 완료")
        void givenJobSeekerProfileUpdateFails_whenDeletePortfolio_thenCompleteSuccessfully() {
            // Given
            testPortfolio.setPortfolioItems(new ArrayList<>());
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(testPortfolio));
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));
            given(jobSeekerProfileRepository.save(any(JobSeekerProfile.class)))
                    .willThrow(new RuntimeException("Database error"));

            // When & Then
            // 예외가 발생하지 않고 정상 완료되어야 함
            assertThatCode(() -> portfolioService.deletePortfolio(TEST_USER_ID))
                    .doesNotThrowAnyException();

            // MongoDB 포트폴리오는 삭제되어야 함
            verify(portfolioRepository).delete(testPortfolio);
        }

        @Test
        @DisplayName("존재하지 않는 포트폴리오 삭제 시도 - 실패")
        void givenNonExistentPortfolio_whenDeletePortfolio_thenThrowException() {
            // Given
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioService.deletePortfolio(TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("포트폴리오를 찾을 수 없습니다");

            verify(portfolioRepository, never()).delete(any());
            verify(jobSeekerProfileRepository, never()).findByUserId(any());
        }

        @Test
        @DisplayName("첨부파일이 있는 포트폴리오 삭제 - 파일도 함께 삭제")
        void givenPortfolioWithAttachments_whenDeletePortfolio_thenDeleteFilesAndPortfolio() {
            // Given
            PortfolioItem itemWithAttachment = PortfolioItem.builder()
                    .id("item-1")
                    .order(1)
                    .type("experience")
                    .title("테스트 경험")
                    .content("테스트 내용")
                    .attachments(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Attachment attachment = Attachment.builder()
                    .filePath("/uploads/test-file.pdf")
                    .extractionStatus("completed")
                    .build();

            itemWithAttachment.getAttachments().add(attachment);
            testPortfolio.setPortfolioItems(new ArrayList<>());
            testPortfolio.getPortfolioItems().add(itemWithAttachment);

            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(testPortfolio));
            given(jobSeekerProfileRepository.findByUserId(TEST_USER_UUID))
                    .willReturn(Optional.of(testJobSeekerProfile));

            // When
            portfolioService.deletePortfolio(TEST_USER_ID);

            // Then
            verify(fileStorageService).deleteFiles(anyList());
            verify(portfolioRepository).delete(testPortfolio);
        }
    }

    @Nested
    @DisplayName("포트폴리오 조회 테스트")
    class GetPortfolioTests {

        @Test
        @DisplayName("포트폴리오 전체 조회 - 성공")
        void givenExistingPortfolio_whenGetMyPortfolio_thenReturnPortfolio() {
            // Given
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.of(testPortfolio));

            // When
            PortfolioResponseDto result = portfolioService.getMyPortfolio(TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getBasicInfo()).isNotNull();
            verify(portfolioRepository).findByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 포트폴리오 조회 - 실패")
        void givenNonExistentPortfolio_whenGetMyPortfolio_thenThrowException() {
            // Given
            given(portfolioRepository.findByUserId(TEST_USER_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioService.getMyPortfolio(TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("포트폴리오를 찾을 수 없습니다");
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .password("encoded-password")
                .role(UserRole.JOB_SEEKER)
                .build();
    }

    private JobSeekerProfile createTestJobSeekerProfile() {
        return JobSeekerProfile.builder()
                .user(testUser)
                .firstName("길동")
                .lastName("홍")
                .phone("010-1234-5678")
                .desiredPosition("백엔드 개발자")
                .portfolioId(null)
                .build();
    }

    private Portfolio createTestPortfolio() {
        BasicInfo basicInfo = BasicInfo.builder()
                .name(TEST_NAME)
                .schoolName(TEST_SCHOOL)
                .major(TEST_MAJOR)
                .gpa(TEST_GPA)
                .desiredPosition("백엔드 개발자")
                .build();

        ProcessingStatus processingStatus = ProcessingStatus.builder()
                .needsEmbedding(true)
                .lastProcessed(null)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .userId(TEST_USER_ID)
                .basicInfo(basicInfo)
                .portfolioItems(new ArrayList<>())
                .embeddings(null)
                .processingStatus(processingStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        portfolio.setId(TEST_PORTFOLIO_ID);
        return portfolio;
    }

    private BasicInfoDto createTestBasicInfoDto() {
        return BasicInfoDto.builder()
                .name(TEST_NAME)
                .schoolName(TEST_SCHOOL)
                .major(TEST_MAJOR)
                .gpa(TEST_GPA)
                .desiredPosition("백엔드 개발자")
                .referenceUrl(List.of("https://github.com/test"))
                .build();
    }
}
