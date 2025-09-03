package com.example.experfolio.global.security.jwt;

import lombok.Builder;
import lombok.Getter;

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
}