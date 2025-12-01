package com.example.experfolio.domain.favorite.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 즐겨찾기 엔티티 (MVP)
 * 리크루터가 구직자의 포트폴리오를 즐겨찾기하는 기능
 * Portfolio 중심 설계로 User FK 제거
 */
@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recruiter_id", nullable = false)
    private String recruiterId;

    @Column(name = "job_seeker_id", nullable = false)
    private String jobSeekerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Favorite(String recruiterId, String jobSeekerId) {
        if (recruiterId == null || recruiterId.isBlank()) {
            throw new IllegalArgumentException("리크루터 ID는 필수입니다");
        }
        if (jobSeekerId == null || jobSeekerId.isBlank()) {
            throw new IllegalArgumentException("구직자 ID는 필수입니다");
        }
        if (recruiterId.equals(jobSeekerId)) {
            throw new IllegalArgumentException("자기 자신을 즐겨찾기할 수 없습니다");
        }

        this.recruiterId = recruiterId;
        this.jobSeekerId = jobSeekerId;
    }
}
