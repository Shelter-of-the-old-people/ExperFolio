package com.example.experfolio.unit.service;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.service.AuthService;
import com.example.experfolio.domain.user.service.UserService;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.UnauthorizedException;
import com.example.experfolio.global.security.jwt.JwtTokenInfo;
import com.example.experfolio.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    // 테스트용 상수
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String NEW_PASSWORD = "newPassword123!";
    private static final UserRole DEFAULT_ROLE = UserRole.JOB_SEEKER;
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "valid-access-token";
    private static final String REFRESH_TOKEN = "valid-refresh-token";
    private static final String VERIFICATION_TOKEN = "email-verification-token";
    private static final String RESET_TOKEN = "password-reset-token";

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private JwtTokenInfo testTokenInfo;

    @BeforeEach
    void setUp() {
        testUser = createValidUser();
        testTokenInfo = createTestTokenInfo();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTests {

        @Test
        @DisplayName("유효한 정보로 회원가입 - 성공")
        void givenValidRegistrationInfo_whenRegister_thenReturnCreatedUser() {
            // Given
            given(userService.isEmailAvailable(VALID_EMAIL)).willReturn(true);
            given(userService.createUser(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE)).willReturn(testUser);

            // When
            User registeredUser = authService.register(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE);

            // Then
            assertThat(registeredUser).isNotNull();
            assertThat(registeredUser.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(registeredUser.getRole()).isEqualTo(DEFAULT_ROLE);

            verify(userService).isEmailAvailable(VALID_EMAIL);
            verify(userService).createUser(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE);
            verify(userService).sendEmailVerificationToken(testUser.getId());
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 - 실패")
        void givenDuplicateEmail_whenRegister_thenThrowBadRequestException() {
            // Given
            given(userService.isEmailAvailable(VALID_EMAIL)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.register(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");

            verify(userService).isEmailAvailable(VALID_EMAIL);
            verify(userService, never()).createUser(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("유효한 자격증명으로 로그인 - 성공")
        void givenValidCredentials_whenLogin_thenReturnTokenInfo() {
            // Given
            testUser.activate();
            testUser.verifyEmail();
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(VALID_EMAIL, DEFAULT_ROLE, testUser.getId()))
                    .willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(VALID_EMAIL, testUser.getId()))
                    .willReturn(REFRESH_TOKEN);
            given(jwtTokenProvider.createTokenInfo(ACCESS_TOKEN, REFRESH_TOKEN))
                    .willReturn(testTokenInfo);

            // When
            JwtTokenInfo tokenInfo = authService.login(VALID_EMAIL, VALID_PASSWORD);

            // Then
            assertThat(tokenInfo).isNotNull();
            assertThat(tokenInfo.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(tokenInfo.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

            verify(userService).findActiveByEmail(VALID_EMAIL);
            verify(passwordEncoder).matches(VALID_PASSWORD, testUser.getPassword());
            verify(userService).updateLastLoginTime(testUser.getId());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 - 실패")
        void givenNonExistentEmail_whenLogin_thenThrowUnauthorizedException() {
            // Given
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");

            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 - 실패")
        void givenIncorrectPassword_whenLogin_thenThrowUnauthorizedException() {
            // Given
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");

            verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
        }

        @Test
        @DisplayName("비활성화된 계정으로 로그인 - 실패")
        void givenInactiveAccount_whenLogin_thenThrowUnauthorizedException() {
            // Given
            testUser.deactivate(); // 계정 비활성화
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("비활성화된 계정입니다");
        }

        @Test
        @DisplayName("이메일 미인증 계정으로 로그인 - 실패")
        void givenUnverifiedEmail_whenLogin_thenThrowUnauthorizedException() {
            // Given
            testUser.activate();
            // testUser.verifyEmail() 호출하지 않음 (미인증 상태)
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(VALID_EMAIL, VALID_PASSWORD))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("이메일 인증이 필요합니다");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("유효한 Refresh Token으로 갱신 - 성공")
        void givenValidRefreshToken_whenRefreshToken_thenReturnNewTokenInfo() {
            // Given
            testUser.activate();
            given(jwtTokenProvider.validateRefreshToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserEmail(REFRESH_TOKEN)).willReturn(VALID_EMAIL);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.createAccessToken(VALID_EMAIL, DEFAULT_ROLE, testUser.getId()))
                    .willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createTokenInfo(ACCESS_TOKEN, REFRESH_TOKEN))
                    .willReturn(testTokenInfo);

            // When
            JwtTokenInfo tokenInfo = authService.refreshToken(REFRESH_TOKEN);

            // Then
            assertThat(tokenInfo).isNotNull();
            assertThat(tokenInfo.getAccessToken()).isEqualTo(ACCESS_TOKEN);

            verify(jwtTokenProvider).validateRefreshToken(REFRESH_TOKEN);
            verify(jwtTokenProvider).createAccessToken(VALID_EMAIL, DEFAULT_ROLE, testUser.getId());
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 갱신 - 실패")
        void givenInvalidRefreshToken_whenRefreshToken_thenThrowUnauthorizedException() {
            // Given
            given(jwtTokenProvider.validateRefreshToken(REFRESH_TOKEN)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("유효하지 않은 Refresh Token입니다");

            verify(jwtTokenProvider, never()).getUserEmail(any());
            verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
        }

        @Test
        @DisplayName("비활성화된 사용자로 토큰 갱신 - 실패")
        void givenInactiveUser_whenRefreshToken_thenThrowUnauthorizedException() {
            // Given
            testUser.deactivate();
            given(jwtTokenProvider.validateRefreshToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserEmail(REFRESH_TOKEN)).willReturn(VALID_EMAIL);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("비활성화된 계정입니다");
        }
    }

    @Nested
    @DisplayName("이메일 인증 테스트")
    class EmailVerificationTests {

        @Test
        @DisplayName("유효한 토큰으로 이메일 인증 - 성공")
        void givenValidToken_whenVerifyEmail_thenVerifySuccessfully() {
            // Given
            willDoNothing().given(userService).verifyEmail(VERIFICATION_TOKEN);

            // When
            authService.verifyEmail(VERIFICATION_TOKEN);

            // Then
            verify(userService).verifyEmail(VERIFICATION_TOKEN);
        }

        @Test
        @DisplayName("이메일 인증 토큰 재전송 - 성공")
        void givenUnverifiedUser_whenResendEmailVerification_thenSendToken() {
            // Given
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));
            willDoNothing().given(userService).sendEmailVerificationToken(testUser.getId());

            // When
            authService.resendEmailVerification(VALID_EMAIL);

            // Then
            verify(userService).findActiveByEmail(VALID_EMAIL);
            verify(userService).sendEmailVerificationToken(testUser.getId());
        }

        @Test
        @DisplayName("이미 인증된 이메일로 재전송 요청 - 실패")
        void givenVerifiedUser_whenResendEmailVerification_thenThrowBadRequestException() {
            // Given
            testUser.verifyEmail();
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.resendEmailVerification(VALID_EMAIL))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 인증된 이메일입니다");

            verify(userService, never()).sendEmailVerificationToken(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 재전송 요청 - 실패")
        void givenNonExistentUser_whenResendEmailVerification_thenThrowBadRequestException() {
            // Given
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.resendEmailVerification(VALID_EMAIL))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("비밀번호 관리 테스트")
    class PasswordManagementTests {

        @Test
        @DisplayName("비밀번호 재설정 요청 - 성공")
        void givenValidEmail_whenRequestPasswordReset_thenSendResetToken() {
            // Given
            willDoNothing().given(userService).requestPasswordReset(VALID_EMAIL);

            // When
            authService.requestPasswordReset(VALID_EMAIL);

            // Then
            verify(userService).requestPasswordReset(VALID_EMAIL);
        }

        @Test
        @DisplayName("유효한 토큰으로 비밀번호 재설정 - 성공")
        void givenValidResetToken_whenResetPassword_thenResetSuccessfully() {
            // Given
            willDoNothing().given(userService).resetPassword(RESET_TOKEN, NEW_PASSWORD);

            // When
            authService.resetPassword(RESET_TOKEN, NEW_PASSWORD);

            // Then
            verify(userService).resetPassword(RESET_TOKEN, NEW_PASSWORD);
        }

        @Test
        @DisplayName("올바른 현재 비밀번호로 변경 - 성공")
        void givenCorrectCurrentPassword_whenChangePassword_thenChangeSuccessfully() {
            // Given
            given(userService.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(true);
            willDoNothing().given(userService).updatePassword(TEST_USER_ID, NEW_PASSWORD);

            // When
            authService.changePassword(TEST_USER_ID, VALID_PASSWORD, NEW_PASSWORD);

            // Then
            verify(passwordEncoder).matches(VALID_PASSWORD, testUser.getPassword());
            verify(userService).updatePassword(TEST_USER_ID, NEW_PASSWORD);
        }

        @Test
        @DisplayName("잘못된 현재 비밀번호로 변경 시도 - 실패")
        void givenIncorrectCurrentPassword_whenChangePassword_thenThrowBadRequestException() {
            // Given
            String incorrectPassword = "wrongpassword";
            given(userService.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(incorrectPassword, testUser.getPassword())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.changePassword(TEST_USER_ID, incorrectPassword, NEW_PASSWORD))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("현재 비밀번호가 올바르지 않습니다");

            verify(userService, never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 비밀번호 변경 - 실패")
        void givenNonExistentUser_whenChangePassword_thenThrowBadRequestException() {
            // Given
            given(userService.findById(TEST_USER_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.changePassword(TEST_USER_ID, VALID_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("토큰 검증 및 사용자 정보 추출 테스트")
    class TokenValidationTests {

        @Test
        @DisplayName("유효한 토큰으로 사용자 정보 조회 - 성공")
        void givenValidToken_whenGetCurrentUser_thenReturnUser() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserEmail(ACCESS_TOKEN)).willReturn(VALID_EMAIL);
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));

            // When
            User currentUser = authService.getCurrentUser(ACCESS_TOKEN);

            // Then
            assertThat(currentUser).isNotNull();
            assertThat(currentUser.getEmail()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 사용자 정보 조회 - 실패")
        void givenInvalidToken_whenGetCurrentUser_thenThrowUnauthorizedException() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.getCurrentUser(ACCESS_TOKEN))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("유효하지 않은 Access Token입니다");
        }

        @Test
        @DisplayName("토큰 유효성 검증 - 성공")
        void givenValidToken_whenIsTokenValid_thenReturnTrue() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);

            // When
            boolean isValid = authService.isTokenValid(ACCESS_TOKEN);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("토큰 유효성 검증 - 실패")
        void givenInvalidToken_whenIsTokenValid_thenReturnFalse() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(false);

            // When
            boolean isValid = authService.isTokenValid(ACCESS_TOKEN);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("토큰에서 사용자 ID 추출 - 성공")
        void givenValidToken_whenGetUserIdFromToken_thenReturnUserId() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(ACCESS_TOKEN)).willReturn(TEST_USER_ID);

            // When
            UUID userId = authService.getUserIdFromToken(ACCESS_TOKEN);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("토큰에서 사용자 이메일 추출 - 성공")
        void givenValidToken_whenGetUserEmailFromToken_thenReturnEmail() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserEmail(ACCESS_TOKEN)).willReturn(VALID_EMAIL);

            // When
            String email = authService.getUserEmailFromToken(ACCESS_TOKEN);

            // Then
            assertThat(email).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("사용자 역할 확인 - true")
        void givenUserWithRole_whenHasRole_thenReturnTrue() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserRole(ACCESS_TOKEN)).willReturn(DEFAULT_ROLE);

            // When
            boolean hasRole = authService.hasRole(ACCESS_TOKEN, DEFAULT_ROLE);

            // Then
            assertThat(hasRole).isTrue();
        }

        @Test
        @DisplayName("사용자 역할 확인 - false")
        void givenUserWithDifferentRole_whenHasRole_thenReturnFalse() {
            // Given
            given(jwtTokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserRole(ACCESS_TOKEN)).willReturn(UserRole.RECRUITER);

            // When
            boolean hasRole = authService.hasRole(ACCESS_TOKEN, DEFAULT_ROLE);

            // Then
            assertThat(hasRole).isFalse();
        }
    }

    @Nested
    @DisplayName("계정 관리 테스트")
    class AccountManagementTests {

        @Test
        @DisplayName("로그아웃 - 성공")
        void givenUserId_whenLogout_thenLogoutSuccessfully() {
            // When & Then (예외가 발생하지 않으면 성공)
            assertThatCode(() -> authService.logout(TEST_USER_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("계정 잠금 - 성공")
        void givenUserId_whenLockAccount_thenLockSuccessfully() {
            // Given
            given(userService.suspendUser(TEST_USER_ID)).willReturn(testUser);

            // When
            authService.lockAccount(TEST_USER_ID);

            // Then
            verify(userService).suspendUser(TEST_USER_ID);
        }

        @Test
        @DisplayName("계정 잠금 해제 - 성공")
        void givenUserId_whenUnlockAccount_thenUnlockSuccessfully() {
            // Given
            given(userService.activateUser(TEST_USER_ID)).willReturn(testUser);

            // When
            authService.unlockAccount(TEST_USER_ID);

            // Then
            verify(userService).activateUser(TEST_USER_ID);
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private User createValidUser() {
        User user = User.builder()
                .email(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .role(DEFAULT_ROLE)
                .build();
        // User의 ID를 설정하기 위해 Reflection 사용하거나 테스트용 메서드 추가 필요
        return user;
    }

    private JwtTokenInfo createTestTokenInfo() {
        return JwtTokenInfo.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(1800L) // 30분
                .build();
    }
}