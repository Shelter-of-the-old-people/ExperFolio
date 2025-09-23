package com.example.experfolio.web.controller;

import com.example.experfolio.domain.user.controller.AuthController;
import com.example.experfolio.domain.user.dto.LoginRequestDto;
import com.example.experfolio.domain.user.dto.SignUpRequestDto;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.service.AuthService;
import com.example.experfolio.domain.user.service.UserService;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.UnauthorizedException;
import com.example.experfolio.global.security.jwt.JwtTokenInfo;
import com.example.experfolio.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@DisplayName("AuthController 웹 계층 테스트") 
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider JwtTokenProvider;

    // 테스트용 상수
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123!";
    private static final String VALID_NAME = "홍길동";
    private static final String VALID_PHONE = "010-1234-5678";
    private static final UserRole DEFAULT_ROLE = UserRole.JOB_SEEKER;
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final String REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @Nested
    @DisplayName("회원가입 API 테스트")
    class SignupTests {

        @Test
        @DisplayName("유효한 회원가입 요청 - 200 OK")
        void givenValidSignupRequest_whenSignup_thenReturnOk() throws Exception {
            // Given
            SignUpRequestDto signupRequest = createValidSignupRequest();
            User createdUser = createTestUser();
            
            given(authService.register(anyString(), anyString(), any(UserRole.class)))
                    .willReturn(createdUser);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.data.name").value(VALID_NAME))
                    .andExpect(jsonPath("$.data.role").value(DEFAULT_ROLE.toString()));

            verify(authService).register(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE);
        }

        @Test
        @DisplayName("비밀번호 불일치로 회원가입 요청 - 400 Bad Request")
        void givenPasswordMismatch_whenSignup_thenReturnBadRequest() throws Exception {
            // Given
            SignUpRequestDto signupRequest = SignUpRequestDto.builder()
                    .email(VALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .confirmPassword("different-password")
                    .name(VALID_NAME)
                    .phoneNumber(VALID_PHONE)
                    .role(DEFAULT_ROLE)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .with(anonymous()) // 익명 사용자로 요청
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("비밀번호와 비밀번호 확인이 일치하지 않습니다."));

            verify(authService, never()).register(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("필수 필드 누락으로 회원가입 요청 - 400 Bad Request")
        void givenMissingRequiredFields_whenSignup_thenReturnBadRequest() throws Exception {
            // Given
            SignUpRequestDto signupRequest = SignUpRequestDto.builder()
                    .email("") // 빈 이메일
                    .password(VALID_PASSWORD)
                    .confirmPassword(VALID_PASSWORD)
                    .role(DEFAULT_ROLE)
                    // name, phoneNumber 누락
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다: " +
                            "phoneNumber - 전화번호는 필수입니다., " +
                            "name - 이름은 필수입니다., " +
                            "email - 이메일은 필수입니다."));

            verify(authService, never()).register(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 회원가입 요청 - 400 Bad Request")
        void givenInvalidEmailFormat_whenSignup_thenReturnBadRequest() throws Exception {
            // Given
            SignUpRequestDto signupRequest = createValidSignupRequest();
            signupRequest.setEmail("invalid-email-format");

            // When & Then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다: email - 올바른 이메일 형식이 아닙니다."));

            verify(authService, never()).register(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 요청 - 409 Conflict")
        void givenDuplicateEmail_whenSignup_thenReturnConflict() throws Exception {
            // Given
            SignUpRequestDto signupRequest = createValidSignupRequest();
            given(authService.register(anyString(), anyString(), any(UserRole.class)))
                    .willThrow(new BadRequestException("이미 사용 중인 이메일입니다"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다"));

            verify(authService).register(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE);
        }
    }

    @Nested
    @DisplayName("로그인 API 테스트")
    class LoginTests {

        @Test
        @DisplayName("유효한 로그인 요청 - 200 OK")
        void givenValidLoginRequest_whenLogin_thenReturnOk() throws Exception {
            // Given
            LoginRequestDto loginRequest = createValidLoginRequest();
            JwtTokenInfo tokenInfo = createTestTokenInfo();
            User mockUser = createTestUser();

            given(authService.login(VALID_EMAIL, VALID_PASSWORD)).willReturn(tokenInfo);
            given(userService.findActiveByEmail(VALID_EMAIL)).willReturn(Optional.of(mockUser));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                    .andExpect(jsonPath("$.data.refreshToken").value(REFRESH_TOKEN))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

            verify(authService).login(VALID_EMAIL, VALID_PASSWORD);
        }

        @Test
        @DisplayName("잘못된 자격증명으로 로그인 요청 - 401 Unauthorized")
        void givenInvalidCredentials_whenLogin_thenReturnUnauthorized() throws Exception {
            // Given
            LoginRequestDto loginRequest = createValidLoginRequest();
            given(authService.login(VALID_EMAIL, VALID_PASSWORD))
                    .willThrow(new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"));

            verify(authService).login(VALID_EMAIL, VALID_PASSWORD);
        }

        @Test
        @DisplayName("필수 필드 누락으로 로그인 요청 - 400 Bad Request")
        void givenMissingFields_whenLogin_thenReturnBadRequest() throws Exception {
            // Given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .email("") // 빈 이메일
                    .password("") // 빈 비밀번호
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("JSON 형식이 잘못된 로그인 요청 - 400 Bad Request")
        void givenMalformedJson_whenLogin_thenReturnBadRequest() throws Exception {
            // Given
            String malformedJson = "{email: 'test@example.com', password: }"; // 잘못된 JSON

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("유효한 Refresh Token으로 갱신 요청 - 200 OK")
        void givenValidRefreshToken_whenRefresh_thenReturnOk() throws Exception {
            // Given
            JwtTokenInfo newTokenInfo = createTestTokenInfo();
            given(authService.refreshToken(REFRESH_TOKEN)).willReturn(newTokenInfo);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + REFRESH_TOKEN + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                    .andExpect(jsonPath("$.data.refreshToken").value(REFRESH_TOKEN));

            verify(authService).refreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 갱신 요청 - 401 Unauthorized")
        void givenInvalidRefreshToken_whenRefresh_thenReturnUnauthorized() throws Exception {
            // Given
            String invalidToken = "invalid-refresh-token";
            given(authService.refreshToken(invalidToken))
                    .willThrow(new UnauthorizedException("유효하지 않은 Refresh Token입니다"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + invalidToken + "\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다"));

            verify(authService).refreshToken(invalidToken);
        }
    }

    @Nested
    @DisplayName("이메일 인증 API 테스트")
    class EmailVerificationTests {

        @Test
        @DisplayName("유효한 토큰으로 이메일 인증 - 200 OK")
        void givenValidVerificationToken_whenVerifyEmail_thenReturnOk() throws Exception {
            // Given
            String verificationToken = "valid-verification-token";
            willDoNothing().given(authService).verifyEmail(verificationToken);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"" + verificationToken + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다"));

            verify(authService).verifyEmail(verificationToken);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 이메일 인증 - 400 Bad Request")
        void givenInvalidVerificationToken_whenVerifyEmail_thenReturnBadRequest() throws Exception {
            // Given
            String invalidToken = "invalid-verification-token";
            willThrow(new BadRequestException("유효하지 않은 인증 토큰입니다"))
                    .given(authService).verifyEmail(invalidToken);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"" + invalidToken + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다"));

            verify(authService).verifyEmail(invalidToken);
        }

        @Test
        @DisplayName("이메일 인증 재전송 - 200 OK")
        void givenValidEmail_whenResendVerification_thenReturnOk() throws Exception {
            // Given
            willDoNothing().given(authService).resendEmailVerification(VALID_EMAIL);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + VALID_EMAIL + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("인증 이메일이 재전송되었습니다"));

            verify(authService).resendEmailVerification(VALID_EMAIL);
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 API 테스트")
    class PasswordResetTests {

        @Test
        @DisplayName("비밀번호 재설정 요청 - 200 OK")
        void givenValidEmail_whenForgotPassword_thenReturnOk() throws Exception {
            // Given
            willDoNothing().given(authService).requestPasswordReset(VALID_EMAIL);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + VALID_EMAIL + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("비밀번호 재설정 이메일이 발송되었습니다"));

            verify(authService).requestPasswordReset(VALID_EMAIL);
        }

        @Test
        @DisplayName("비밀번호 재설정 - 200 OK")
        void givenValidResetToken_whenResetPassword_thenReturnOk() throws Exception {
            // Given
            String resetToken = "valid-reset-token";
            String newPassword = "newPassword123!";
            willDoNothing().given(authService).resetPassword(resetToken, newPassword);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"" + resetToken + "\",\"newPassword\":\"" + newPassword + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 재설정되었습니다"));

            verify(authService).resetPassword(resetToken, newPassword);
        }
    }

    @Nested
    @DisplayName("현재 사용자 정보 조회 API 테스트")
    class GetCurrentUserTests {

        @Test
        @DisplayName("유효한 토큰으로 현재 사용자 정보 조회 - 200 OK")
        void givenValidAccessToken_whenGetMe_thenReturnOk() throws Exception {
            // Given
            User currentUser = createTestUser();
            given(authService.getCurrentUser(ACCESS_TOKEN)).willReturn(currentUser);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/me")
                            .header("Authorization", "Bearer " + ACCESS_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.data.name").value(VALID_NAME))
                    .andExpect(jsonPath("$.data.role").value(DEFAULT_ROLE.toString()));

            verify(authService).getCurrentUser(ACCESS_TOKEN);
        }

        @Test
        @DisplayName("토큰 없이 현재 사용자 정보 조회 - 401 Unauthorized")
        void givenNoToken_whenGetMe_thenReturnUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized());

            verify(authService, never()).getCurrentUser(anyString());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 현재 사용자 정보 조회 - 401 Unauthorized")
        void givenInvalidToken_whenGetMe_thenReturnUnauthorized() throws Exception {
            // Given
            String invalidToken = "invalid-access-token";
            given(authService.getCurrentUser(invalidToken))
                    .willThrow(new UnauthorizedException("유효하지 않은 Access Token입니다"));

            // When & Then
            mockMvc.perform(get("/api/v1/auth/me")
                            .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 Access Token입니다"));

            verify(authService).getCurrentUser(invalidToken);
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드들
    private SignUpRequestDto createValidSignupRequest() {
        return SignUpRequestDto.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .confirmPassword(VALID_PASSWORD)
                .name(VALID_NAME)
                .phoneNumber(VALID_PHONE)
                .role(DEFAULT_ROLE)
                .build();
    }

    private LoginRequestDto createValidLoginRequest() {
        return LoginRequestDto.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .email(VALID_EMAIL)
                .password("encoded-password")
                .name(VALID_NAME)
                .phoneNumber(VALID_PHONE)
                .role(DEFAULT_ROLE)
                .build();
    }

    private JwtTokenInfo createTestTokenInfo() {
        return JwtTokenInfo.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .tokenType("Bearer")
                .expiresIn(1800L) // 30분
                .accessTokenExpiresAt(LocalDateTime.now().plusMinutes(30))
                .refreshTokenExpiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }
}