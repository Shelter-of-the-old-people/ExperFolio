package com.example.experfolio.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recruiter_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruiterProfile {

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

    @Column(length = 20)
    private String phone;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_description")
    private String companyDescription;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String position;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "is_company_verified", nullable = false)
    private Boolean isCompanyVerified = false;

    @Column(name = "company_verification_document_url")
    private String companyVerificationDocumentUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RecruiterProfile(User user, String firstName, String lastName, String phone,
                           String companyName, String companyWebsite, String companyDescription,
                           String department, String position, String businessRegistrationNumber,
                           String companyVerificationDocumentUrl) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.companyDescription = companyDescription;
        this.department = department;
        this.position = position;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.companyVerificationDocumentUrl = companyVerificationDocumentUrl;
        this.isCompanyVerified = false;
    }

    // 비즈니스 메서드
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateBasicInfo(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public void updateCompanyInfo(String companyName, String companyWebsite, 
                                 String companyDescription, String department, String position) {
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.companyDescription = companyDescription;
        this.department = department;
        this.position = position;
    }

    public void updateVerificationInfo(String businessRegistrationNumber, 
                                      String companyVerificationDocumentUrl) {
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.companyVerificationDocumentUrl = companyVerificationDocumentUrl;
    }

    public void verifyCompany() {
        this.isCompanyVerified = true;
    }

    public void unverifyCompany() {
        this.isCompanyVerified = false;
    }

    public boolean isProfileComplete() {
        return firstName != null && lastName != null && companyName != null;
    }

    public boolean hasVerificationDocuments() {
        return businessRegistrationNumber != null || companyVerificationDocumentUrl != null;
    }
}