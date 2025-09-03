package com.example.experfolio.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    PENDING("승인 대기"),
    SUSPENDED("일시 정지");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }

}