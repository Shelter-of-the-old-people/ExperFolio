package com.example.experfolio.global.security.jwt;

import com.example.experfolio.domain.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    // Access Token 생성
    public String createAccessToken(String email, UserRole role, UUID userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .claim("userId", userId.toString())
                .claim("tokenType", "ACCESS")
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String email, UUID userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("tokenType", "REFRESH")
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // JWT 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        
        if (claims.get("role") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(new String[]{claims.get("role").toString()})
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // JWT 토큰에서 사용자 ID 추출
    public UUID getUserId(String token) {
        Claims claims = parseClaims(token);
        String userIdStr = claims.get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    // JWT 토큰에서 사용자 역할 추출
    public UserRole getUserRole(String token) {
        Claims claims = parseClaims(token);
        String roleStr = claims.get("role", String.class);
        return UserRole.valueOf(roleStr);
    }

    // JWT 토큰에서 토큰 타입 추출
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tokenType", String.class);
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        }
        return false;
    }

    // Access Token 유효성 검증
    public boolean validateAccessToken(String token) {
        boolean isValidToken = validateToken(token);
        if (!isValidToken) {
            return false;
        }
        
        String tokenType = getTokenType(token);
        return "ACCESS".equals(tokenType);
    }

    // Refresh Token 유효성 검증
    public boolean validateRefreshToken(String token) {
        boolean isValidToken = validateToken(token);
        if (!isValidToken) {
            return false;
        }
        
        String tokenType = getTokenType(token);
        return "REFRESH".equals(tokenType);
    }

    // JWT 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // JWT 토큰의 남은 유효 시간 (초 단위)
    public long getRemainingTime(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return (expiration.getTime() - now.getTime()) / 1000;
    }

    // JWT 토큰의 발급 시간 조회
    public LocalDateTime getIssuedAt(String token) {
        Claims claims = parseClaims(token);
        Date issuedAt = claims.getIssuedAt();
        return issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // JWT 토큰의 만료 시간 조회
    public LocalDateTime getExpirationTime(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Refresh Token으로 새로운 Access Token 생성
    public String refreshAccessToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        Claims claims = parseClaims(refreshToken);
        String email = claims.getSubject();
        UUID userId = UUID.fromString(claims.get("userId", String.class));
        
        // 원본 사용자 정보를 조회하여 현재 역할 정보로 새 토큰 생성
        // 실제로는 UserService를 주입받아 사용자 정보를 조회해야 합니다.
        // 여기서는 예시로 JOB_SEEKER로 하드코딩했습니다.
        UserRole role = UserRole.JOB_SEEKER; // TODO: 실제 사용자 역할 조회 로직 필요
        
        return createAccessToken(email, role, userId);
    }

    // JWT Claims 파싱
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 정보를 담은 DTO 생성
    public JwtTokenInfo createTokenInfo(String accessToken, String refreshToken) {
        LocalDateTime accessTokenExpiresAt = getExpirationTime(accessToken);
        LocalDateTime refreshTokenExpiresAt = getExpirationTime(refreshToken);
        
        return JwtTokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenValidityInMilliseconds / 1000)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .build();
    }
}