package com.example.experfolio.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    JOB_SEEKER("구직자"),
    RECRUITER("채용담당자"),
    ADMIN("관리자");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }

}