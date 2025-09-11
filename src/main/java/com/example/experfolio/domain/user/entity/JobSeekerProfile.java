package com.example.experfolio.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_seeker_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobSeekerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 10)
    private String gender;

    private String address;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    // 포트폴리오 기본 정보
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "career_objective", columnDefinition = "TEXT")
    private String careerObjective;

    @Column(name = "desired_position")
    private String desiredPosition;

    // 희망 최소 연봉
    @Column(name = "desired_salary_min")
    private Integer desiredSalaryMin;

    // 희망 최대 연봉
    @Column(name = "desired_salary_max")
    private Integer desiredSalaryMax;

    @Column(name = "desired_location")
    private String desiredLocation;

    @Column(name = "available_start_date")
    private LocalDate availableStartDate;

    // 추가 정보 - URL 리스트 (GitHub, 포트폴리오, LinkedIn 등)
    @ElementCollection
    @CollectionTable(name = "job_seeker_urls", joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Column(name = "url")
    private List<String> urls = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public JobSeekerProfile(User user, String firstName, String lastName, String phone, 
                           LocalDate birthDate, String gender, String address, String state, String postalCode, String profileImageUrl, String summary,
                           String careerObjective, String desiredPosition, Integer desiredSalaryMin, 
                           Integer desiredSalaryMax, String desiredLocation, 
                           LocalDate availableStartDate, List<String> urls) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.state = state;
        this.postalCode = postalCode;
        this.profileImageUrl = profileImageUrl;
        this.summary = summary;
        this.careerObjective = careerObjective;
        this.desiredPosition = desiredPosition;
        this.desiredSalaryMin = desiredSalaryMin;
        this.desiredSalaryMax = desiredSalaryMax;
        this.desiredLocation = desiredLocation;
        this.availableStartDate = availableStartDate;
        this.urls = urls != null ? new ArrayList<>(urls) : new ArrayList<>();
    }

    // 비즈니스 메서드
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateBasicInfo(String firstName, String lastName, String phone, 
                               LocalDate birthDate, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void updateAddress(String address, String state, String postalCode) {
        this.address = address;
        this.state = state;
        this.postalCode = postalCode;
    }

    public void updateProfileInfo(String profileImageUrl, String summary, String careerObjective) {
        this.profileImageUrl = profileImageUrl;
        this.summary = summary;
        this.careerObjective = careerObjective;
    }

    public void updateDesiredConditions(String desiredPosition, Integer desiredSalaryMin, 
                                       Integer desiredSalaryMax, String desiredLocation, 
                                       LocalDate availableStartDate) {
        this.desiredPosition = desiredPosition;
        this.desiredSalaryMin = desiredSalaryMin;
        this.desiredSalaryMax = desiredSalaryMax;
        this.desiredLocation = desiredLocation;
        this.availableStartDate = availableStartDate;
    }

    public void addUrl(String url) {
        if (url != null && !url.trim().isEmpty() && !this.urls.contains(url)) {
            this.urls.add(url);
        }
    }

    public void removeUrl(String url) {
        this.urls.remove(url);
    }

    public void updateUrls(List<String> urls) {
        this.urls.clear();
        if (urls != null) {
            this.urls.addAll(urls);
        }
    }

    public boolean hasSalaryRange() {
        return desiredSalaryMin != null && desiredSalaryMax != null;
    }

    public boolean isProfileComplete() {
        return firstName != null && lastName != null && 
               summary != null && desiredPosition != null;
    }
}