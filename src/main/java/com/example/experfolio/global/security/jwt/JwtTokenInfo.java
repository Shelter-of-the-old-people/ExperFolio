package com.example.experfolio.global.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class JwtTokenInfo {
    
    // Access Token
    private String accessToken;
    
    // Refresh Token
    private String refreshToken;
    
    // 토큰 타입 (Bearer)
    private String tokenType;
    
    // Access Token 만료 시간 (초 단위)
    private Long expiresIn;
    
    // Access Token 만료 일시
    private LocalDateTime accessTokenExpiresAt;
    
    // Refresh Token 만료 일시
    private LocalDateTime refreshTokenExpiresAt;
}