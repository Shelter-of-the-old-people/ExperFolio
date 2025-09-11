package com.example.experfolio.unit.security.jwt;

import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.global.security.jwt.JwtTokenInfo;
import com.example.experfolio.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    // 테스트용 상수
    private static final String SECRET_KEY = "mySecretKeyForTestingPurposesShouldBeAtLeast32Characters1234567890";
    private static final long ACCESS_TOKEN_VALIDITY = 1800; // 30분
    private static final long REFRESH_TOKEN_VALIDITY = 604800; // 7일
    private static final String TEST_EMAIL = "test@example.com";
    private static final UserRole TEST_ROLE = UserRole.JOB_SEEKER;
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                SECRET_KEY,
                ACCESS_TOKEN_VALIDITY,
                REFRESH_TOKEN_VALIDITY
        );
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenCreationTests {

        @Test
        @DisplayName("유효한 정보로 Access Token 생성 - 성공")
        void givenValidUserInfo_whenCreateAccessToken_thenReturnValidToken() {
            // When
            String accessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);

            // Then
            assertThat(accessToken).isNotNull();
            assertThat(accessToken).isNotEmpty();
            assertThat(accessToken.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성

            // 토큰이 유효한지 검증
            assertThat(jwtTokenProvider.validateAccessToken(accessToken)).isTrue();
            
            // 토큰에서 정보 추출 검증
            assertThat(jwtTokenProvider.getUserEmail(accessToken)).isEqualTo(TEST_EMAIL);
            assertThat(jwtTokenProvider.getUserRole(accessToken)).isEqualTo(TEST_ROLE);
            assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(TEST_USER_ID);
            assertThat(jwtTokenProvider.getTokenType(accessToken)).isEqualTo("ACCESS");
        }

        @Test
        @DisplayName("유효한 정보로 Refresh Token 생성 - 성공")
        void givenValidUserInfo_whenCreateRefreshToken_thenReturnValidToken() {
            // When
            String refreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);

            // Then
            assertThat(refreshToken).isNotNull();
            assertThat(refreshToken).isNotEmpty();
            assertThat(refreshToken.split("\\.")).hasSize(3);

            // 토큰이 유효한지 검증
            assertThat(jwtTokenProvider.validateRefreshToken(refreshToken)).isTrue();
            
            // 토큰에서 정보 추출 검증
            assertThat(jwtTokenProvider.getUserEmail(refreshToken)).isEqualTo(TEST_EMAIL);
            assertThat(jwtTokenProvider.getUserId(refreshToken)).isEqualTo(TEST_USER_ID);
            assertThat(jwtTokenProvider.getTokenType(refreshToken)).isEqualTo("REFRESH");
        }

        @Test
        @DisplayName("JwtTokenInfo 생성 - 성공")
        void givenTokens_whenCreateTokenInfo_thenReturnTokenInfo() {
            // Given
            String accessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
            String refreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);

            // When
            JwtTokenInfo tokenInfo = jwtTokenProvider.createTokenInfo(accessToken, refreshToken);

            // Then
            assertThat(tokenInfo).isNotNull();
            assertThat(tokenInfo.getAccessToken()).isEqualTo(accessToken);
            assertThat(tokenInfo.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(tokenInfo.getTokenType()).isEqualTo("Bearer");
            assertThat(tokenInfo.getExpiresIn()).isEqualTo(ACCESS_TOKEN_VALIDITY);
            assertThat(tokenInfo.getAccessTokenExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(tokenInfo.getRefreshTokenExpiresAt()).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTests {

        private String validAccessToken;
        private String validRefreshToken;

        @BeforeEach
        void setUp() {
            validAccessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
            validRefreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);
        }

        @Test
        @DisplayName("유효한 Access Token 검증 - true")
        void givenValidAccessToken_whenValidateAccessToken_thenReturnTrue() {
            // When
            boolean isValid = jwtTokenProvider.validateAccessToken(validAccessToken);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효한 Refresh Token 검증 - true")
        void givenValidRefreshToken_whenValidateRefreshToken_thenReturnTrue() {
            // When
            boolean isValid = jwtTokenProvider.validateRefreshToken(validRefreshToken);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 - false")
        void givenMalformedToken_whenValidateToken_thenReturnFalse() {
            // Given
            String malformedToken = "invalid.token.format";

            // When
            boolean isValid = jwtTokenProvider.validateToken(malformedToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null 토큰 검증 - false")
        void givenNullToken_whenValidateToken_thenReturnFalse() {
            // When
            boolean isValid = jwtTokenProvider.validateToken(null);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 - false")
        void givenEmptyToken_whenValidateToken_thenReturnFalse() {
            // When
            boolean isValid = jwtTokenProvider.validateToken("");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("잘못된 타입의 토큰으로 Access Token 검증 - false")
        void givenRefreshTokenType_whenValidateAccessToken_thenReturnFalse() {
            // When
            boolean isValid = jwtTokenProvider.validateAccessToken(validRefreshToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("잘못된 타입의 토큰으로 Refresh Token 검증 - false")
        void givenAccessTokenType_whenValidateRefreshToken_thenReturnFalse() {
            // When
            boolean isValid = jwtTokenProvider.validateRefreshToken(validAccessToken);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 정보 추출 테스트")
    class TokenExtractionTests {

        private String validAccessToken;
        private String validRefreshToken;

        @BeforeEach
        void setUp() {
            validAccessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
            validRefreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);
        }

        @Test
        @DisplayName("토큰에서 사용자 이메일 추출 - 성공")
        void givenValidToken_whenGetUserEmail_thenReturnEmail() {
            // When
            String email = jwtTokenProvider.getUserEmail(validAccessToken);

            // Then
            assertThat(email).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("토큰에서 사용자 ID 추출 - 성공")
        void givenValidToken_whenGetUserId_thenReturnUserId() {
            // When
            UUID userId = jwtTokenProvider.getUserId(validAccessToken);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Access Token에서 사용자 역할 추출 - 성공")
        void givenValidAccessToken_whenGetUserRole_thenReturnRole() {
            // When
            UserRole role = jwtTokenProvider.getUserRole(validAccessToken);

            // Then
            assertThat(role).isEqualTo(TEST_ROLE);
        }

        @Test
        @DisplayName("Access Token에서 토큰 타입 추출 - 성공")
        void givenValidAccessToken_whenGetTokenType_thenReturnAccessType() {
            // When
            String tokenType = jwtTokenProvider.getTokenType(validAccessToken);

            // Then
            assertThat(tokenType).isEqualTo("ACCESS");
        }

        @Test
        @DisplayName("Refresh Token에서 토큰 타입 추출 - 성공")
        void givenValidRefreshToken_whenGetTokenType_thenReturnRefreshType() {
            // When
            String tokenType = jwtTokenProvider.getTokenType(validRefreshToken);

            // Then
            assertThat(tokenType).isEqualTo("REFRESH");
        }

        @Test
        @DisplayName("Refresh Token에서 역할 추출 시도 - null 반환 또는 예외 발생")
        void givenRefreshToken_whenGetUserRole_thenThrowExceptionOrReturnNull() {
            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.getUserRole(validRefreshToken))
                    .isInstanceOfAny(IllegalArgumentException.class, NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Authentication 객체 생성 테스트")
    class AuthenticationTests {

        private String validAccessToken;

        @BeforeEach
        void setUp() {
            validAccessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
        }

        @Test
        @DisplayName("유효한 토큰으로 Authentication 생성 - 성공")
        void givenValidToken_whenGetAuthentication_thenReturnAuthentication() {
            // When
            Authentication authentication = jwtTokenProvider.getAuthentication(validAccessToken);

            // Then
            assertThat(authentication).isNotNull();
            assertThat(authentication.getName()).isEqualTo(TEST_EMAIL);
            assertThat(authentication.isAuthenticated()).isTrue();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_" + TEST_ROLE.name());
        }

        @Test
        @DisplayName("역할 정보가 없는 토큰으로 Authentication 생성 시도 - 예외 발생")
        void givenTokenWithoutRole_whenGetAuthentication_thenThrowException() {
            // Given
            String refreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(refreshToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("권한 정보가 없는 토큰입니다");
        }
    }

    @Nested
    @DisplayName("토큰 시간 관련 테스트")
    class TokenTimeTests {

        private String validAccessToken;

        @BeforeEach
        void setUp() {
            validAccessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
        }

        @Test
        @DisplayName("토큰 만료 여부 확인 - false (새로 생성된 토큰)")
        void givenNewToken_whenIsTokenExpired_thenReturnFalse() {
            // When
            boolean isExpired = jwtTokenProvider.isTokenExpired(validAccessToken);

            // Then
            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("토큰의 남은 유효 시간 조회 - 양수 값")
        void givenValidToken_whenGetRemainingTime_thenReturnPositiveValue() {
            // When
            long remainingTime = jwtTokenProvider.getRemainingTime(validAccessToken);

            // Then
            assertThat(remainingTime).isPositive();
            assertThat(remainingTime).isLessThanOrEqualTo(ACCESS_TOKEN_VALIDITY);
        }

        @Test
        @DisplayName("토큰의 발급 시간 조회 - 현재 시간과 근사")
        void givenValidToken_whenGetIssuedAt_thenReturnRecentTime() {
            // When
            LocalDateTime issuedAt = jwtTokenProvider.getIssuedAt(validAccessToken);

            // Then
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt).isBefore(LocalDateTime.now().plusSeconds(1)); // 1초 여유
            assertThat(issuedAt).isAfter(LocalDateTime.now().minusMinutes(1)); // 1분 전까지 허용
        }

        @Test
        @DisplayName("토큰의 만료 시간 조회 - 미래 시간")
        void givenValidToken_whenGetExpirationTime_thenReturnFutureTime() {
            // When
            LocalDateTime expirationTime = jwtTokenProvider.getExpirationTime(validAccessToken);

            // Then
            assertThat(expirationTime).isNotNull();
            assertThat(expirationTime).isAfter(LocalDateTime.now());
            
            // 대략적인 만료 시간 검증 (30분 후)
            LocalDateTime expectedExpiration = LocalDateTime.now().plusSeconds(ACCESS_TOKEN_VALIDITY);
            assertThat(expirationTime).isBefore(expectedExpiration.plusMinutes(1)); // 1분 여유
            assertThat(expirationTime).isAfter(expectedExpiration.minusMinutes(1)); // 1분 여유
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class TokenRefreshTests {

        private String validRefreshToken;

        @BeforeEach
        void setUp() {
            validRefreshToken = jwtTokenProvider.createRefreshToken(TEST_EMAIL, TEST_USER_ID);
        }

        @Test
        @DisplayName("유효한 Refresh Token으로 Access Token 갱신 - 성공")
        void givenValidRefreshToken_whenRefreshAccessToken_thenReturnNewAccessToken() {
            // When
            String newAccessToken = jwtTokenProvider.refreshAccessToken(validRefreshToken);

            // Then
            assertThat(newAccessToken).isNotNull();
            assertThat(newAccessToken).isNotEmpty();
            assertThat(jwtTokenProvider.validateAccessToken(newAccessToken)).isTrue();
            
            // 새 토큰에서 정보 추출 검증
            assertThat(jwtTokenProvider.getUserEmail(newAccessToken)).isEqualTo(TEST_EMAIL);
            assertThat(jwtTokenProvider.getUserId(newAccessToken)).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 갱신 시도 - 예외 발생")
        void givenInvalidRefreshToken_whenRefreshAccessToken_thenThrowException() {
            // Given
            String invalidToken = "invalid.refresh.token";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.refreshAccessToken(invalidToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("유효하지 않은 Refresh Token입니다");
        }

        @Test
        @DisplayName("Access Token으로 갱신 시도 - 예외 발생")
        void givenAccessToken_whenRefreshAccessToken_thenThrowException() {
            // Given
            String accessToken = jwtTokenProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.refreshAccessToken(accessToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("유효하지 않은 Refresh Token입니다");
        }
    }

    @Nested
    @DisplayName("에지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("매우 짧은 만료 시간의 토큰 생성 - 즉시 만료")
        void givenVeryShortValidity_whenCreateToken_thenTokenExpiresQuickly() throws InterruptedException {
            // Given
            JwtTokenProvider shortValidityProvider = new JwtTokenProvider(SECRET_KEY, 1, 1); // 1초 유효

            // When
            String shortToken = shortValidityProvider.createAccessToken(TEST_EMAIL, TEST_ROLE, TEST_USER_ID);
            
            // 1.5초 대기
            Thread.sleep(1500);

            // Then
            assertThat(shortValidityProvider.isTokenExpired(shortToken)).isTrue();
            assertThat(shortValidityProvider.validateAccessToken(shortToken)).isFalse();
        }

        @Test
        @DisplayName("특수 문자가 포함된 이메일로 토큰 생성 - 성공")
        void givenEmailWithSpecialCharacters_whenCreateToken_thenSuccess() {
            // Given
            String specialEmail = "test+special@sub-domain.example.com";

            // When
            String token = jwtTokenProvider.createAccessToken(specialEmail, TEST_ROLE, TEST_USER_ID);

            // Then
            assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue();
            assertThat(jwtTokenProvider.getUserEmail(token)).isEqualTo(specialEmail);
        }

        @Test
        @DisplayName("매우 긴 이메일로 토큰 생성 - 성공")
        void givenVeryLongEmail_whenCreateToken_thenSuccess() {
            // Given
            String longEmail = "very.very.very.long.email.address.for.testing.purposes@extremely.long.domain.name.example.com";

            // When
            String token = jwtTokenProvider.createAccessToken(longEmail, TEST_ROLE, TEST_USER_ID);

            // Then
            assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue();
            assertThat(jwtTokenProvider.getUserEmail(token)).isEqualTo(longEmail);
        }

        @Test
        @DisplayName("다른 역할들로 토큰 생성 - 모두 성공")
        void givenDifferentRoles_whenCreateTokens_thenAllSuccess() {
            // Given & When & Then
            for (UserRole role : UserRole.values()) {
                String token = jwtTokenProvider.createAccessToken(TEST_EMAIL, role, TEST_USER_ID);
                
                assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue();
                assertThat(jwtTokenProvider.getUserRole(token)).isEqualTo(role);
            }
        }
    }
}