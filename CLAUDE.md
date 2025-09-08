# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Experfolio is a Spring Boot 3.5.5 application built with Java 21 and Gradle. AI-powered portfolio scanning and search platform with JWT authentication. The application serves as a comprehensive job portfolio management and recruitment matching service, utilizing advanced AI technologies including RAG (Retrieval-Augmented Generation) and vector similarity search.

## Build System and Commands

### Gradle Commands
- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew bootRun`
- **Run tests**: `./gradlew test`
- **Clean build artifacts**: `./gradlew clean`
- **Generate dependency reports**: `./gradlew dependencies`

### Development
- **Continuous build**: `./gradlew build --continuous`
- **Run with profile**: `./gradlew bootRun --args='--spring.profiles.active=dev'`

### API Documentation
- **Swagger UI**: Access API documentation at `/swagger-ui.html` when application is running
- **API Specification**: Generate and maintain API specs using Swagger/OpenAPI

### Environment Configuration
- **Environment Variables**: Configure using `.env` file for local development
- **Profile-based Configuration**: Use Spring profiles for different environments

## Core Features

### User Management
- **Registration/Login**: Role selection (Job Seeker/Recruiter)
- **JWT Authentication**: Token-based authentication and authorization
- **Role-based Access Control**: Different permissions for job seekers and recruiters

### Job Seeker Features
- **Portfolio Management**: Personal info, education, experience, tech stack, certifications, desired position, cover letter
- **File Upload**: PDF upload with OCR text extraction, image upload
- **Profile Creation**: Comprehensive job seeker profiles

### Recruiter Features
- **AI-powered Search**: Natural language search capabilities
- **RAG Technology**: Retrieval-Augmented Generation for semantic search with scoring system
- **Advanced Filtering**: Filter by tech stack, experience, position
- **Profile Viewing**: Limited access to job seeker information (privacy protection)
- **Contact Feature**: Email contact functionality

### AI Technologies
- **Vector Similarity Search**: For matching job seekers with requirements
- **OCR Processing**: Automatic text extraction from uploaded documents
- **Natural Language Processing**: For search queries and content analysis

## Architecture and Structure

### Expected Package Structure
- **Base package**: `com.example.experfolio`
- **Controllers**: REST API endpoints for web interface
- **Services**: Business logic for user management, search, AI processing
- **Repositories**: Data access layer
- **Models/Entities**: User, Portfolio, Company data models
- **Security**: JWT authentication and authorization
- **AI/Search**: RAG implementation and vector search
- **File Processing**: OCR and file handling

### Technology Stack

#### Backend Framework
- **Spring Boot**: Main application framework
- **Spring Data JPA**: Database operations and ORM
- **Spring Security**: Authentication and authorization

#### Database
- **PostgreSQL**: Primary database for persistent data storage
- **Redis**: Caching layer for session management and performance optimization

#### Infrastructure
- **AWS**: Cloud infrastructure and services
- **Docker**: Containerization for deployment

#### Key Dependencies (Expected)
- **Spring Boot Web**: REST API development
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **JWT**: Token-based authentication
- **AI/ML Libraries**: For RAG and vector search implementation
- **OCR Libraries**: For document text extraction
- **File Processing**: PDF and image handling
- **Swagger/OpenAPI**: API documentation

### Configuration
- **Application properties**: `src/main/resources/application.properties`
- **Environment variables**: `.env` file for local development settings
- **Database configuration**: PostgreSQL and Redis connection settings
- **AI service configuration**: Settings for RAG and vector search
- **File upload configuration**: Storage and processing settings
- **Security configuration**: JWT and role-based access settings
- **AWS configuration**: Cloud services and Docker deployment settings

## Development Environment

### IDE Support
- **IntelliJ IDEA**: Project is configured with .idea directory
- **Multiple IDE support**: .gitignore includes STS, IntelliJ, NetBeans, and VS Code

### Testing
- **Framework**: JUnit 5 (JUnit Platform)
- **Test runner**: `./gradlew test`
- **Basic context test**: Included in `ExperfolioApplicationTests.java`

## Development Guidelines

### User Roles
- **Job Seekers**: Can create profiles, upload documents, manage portfolio information
- **Recruiters**: Can search candidates, view profiles (with privacy restrictions), contact candidates

### User Role Constraints
- **Strict Role Separation**: Complete functional separation between job seekers and recruiters
- **Access Control**: Job seekers cannot access recruiter features and vice versa
- **Permission Management**: Role-based authorization at both API and UI levels

### Privacy Considerations
- **Limited Information Display**: Show only surnames, approximate addresses
- **Contact Protection**: Email provided only through contact feature
- **Data Security**: Secure handling of personal and professional information

### Search and Matching
- **Natural Language Queries**: Support conversational search inputs
- **Semantic Search**: RAG-based matching beyond keyword matching
- **Scoring System**: Relevance scoring for search results
- **Multi-criteria Filtering**: Tech stack, experience level, position type

## Application Flow

### Recruiter Workflow
1. **Search Interface**: Enter natural language search prompts
2. **LLM Processing**: Process and understand the search intent
3. **RAG Embedding**: Convert prompt to vector embeddings
4. **Retrieval**: Fetch similar candidate profiles using vector similarity
5. **LLM Generation**: Combine prompt context with retrieved data
6. **Results Display**: Present ranked candidate matches with relevance scores

### Job Seeker Workflow
1. **Profile Input**: Enter personal and professional information
2. **Data Processing**: Send profile data to RAG system
3. **Data Preprocessing**: Clean and structure the input data
4. **Vector Storage**: Generate embeddings and store in vector database for future matching

## Project Status

**Current Implementation Status (Phase 2 Complete):**
- ✅ **Basic Spring Boot setup** completed
- ✅ **Domain Layer** implemented (User, JobSeekerProfile, RecruiterProfile entities)
- ✅ **Repository Layer** completed with custom queries and Korean comments
- ✅ **Service Layer** fully implemented with comprehensive business logic
- ✅ **Security Layer** configured with JWT authentication and authorization
- ✅ **Database Schema** designed and DDL generated (PostgreSQL)
- ✅ **Configuration Management** with profile-based settings (dev/prod/test)
- ✅ **Dependency Security** updated (resolved CVE vulnerabilities)
- ✅ **Git Repository** established with structured commits

**Technology Stack Implementation Status:**
- ✅ Spring Boot 3.5.5 + Java 21 + Gradle
- ✅ Spring Data JPA + PostgreSQL + Redis
- ✅ Spring Security + JWT (JJWT 0.12.6)
- ✅ Swagger/OpenAPI documentation ready
- ✅ Docker containerization configured
- ✅ Environment-based configuration (.env + profiles)

**Next Phase Ready:**
- 🔄 Controller Layer implementation
- 🔄 AI/RAG system integration
- 🔄 OCR processing implementation
- 🔄 Vector database integration
- 🔄 Frontend development

## Development Priorities

### ✅ Completed Tasks
- ✅ Environment configuration with `.env` file
- ✅ Swagger/OpenAPI configuration
- ✅ PostgreSQL and Redis connection setup
- ✅ Docker containerization setup
- ✅ JWT authentication system implementation
- ✅ User management with role-based access control
- ✅ Complete domain modeling and database schema
- ✅ Comprehensive service layer with business logic
- ✅ Security configuration with CORS and exception handling

### 🔄 Next Implementation Phase (Phase 3)
- **Controller Layer**: REST API endpoints for all services
- **Validation Layer**: Request/Response DTOs with validation
- **Exception Handling**: Global error handling and custom exceptions
- **Testing**: Unit tests and integration tests
- **API Documentation**: Complete Swagger documentation

### 🔄 Core AI Implementation (Phase 4)
- **RAG system integration** for semantic search
- **OCR processing** for document handling
- **Vector database setup** for similarity search
- **File upload and processing** system
- **AWS deployment** configuration

## Additional Update Requirements

- Swagger를 통한 API 명세
- 환경 변수 .env 파일 생성

## Constraints Overview (Brief)

### User-specific Features & Permission Management
- 구직자(학생), 인사담당자(채용담당자) 사용자별 기능을 완전 분리하여 관리
- 구직자는 기업 사용자의 기능과 권한을 사용할 수 없음

### Backend Technology Stack

- Spring Boot
- JPA
- AWS + Docker

## Database
- PostgreSQL
- Redis

## Detailed Expected Flow

### Recruiter (채용담당자)
1. 검색화면 - 프롬프트 입력
2. LLM - 프롬프트 입력 받고
3. RAG - 프롬프트 임베딩
4. RAG (리트리버) - 프롬프트와 유사한 데이터 가져옴
5. LLM - 프롬프트 + 리트리버 결과
6. 검색 결과 화면 - 프롬프트 결과

### Job Seeker (구직자/학생)
1. 정보 입력 페이지 - 자료 입력
2. 백 - RAG로 데이터 전달
3. RAG - 데이터 전처리
4. RAG - 임베딩 데이터 벡터 데이터 베이스에 저장

## Update History

### CLAUDE.md File 2nd Update Completed

#### Major Changes

1. **Technology Stack Section Greatly Expanded**
   - Backend Framework: Spring Boot, Spring Data JPA, Spring Security
   - Database: PostgreSQL (Primary Database), Redis (Caching and Session Management)
   - Infrastructure: AWS, Docker Containerization
   - API Documentation: Added Swagger/OpenAPI

2. **Build and Development Commands Expanded**
   - Added Swagger UI access method (`/swagger-ui.html`)
   - Environment Variable Setup Guide (.env file)
   - Profile-based Configuration Management

3. **User Role Constraints Section Established**
   - Complete separation of job seeker and recruiter functions
   - Strict management of role-based access permissions
   - Permission management at both API and UI levels

4. **Application Flow Section Added**
   - **Recruiter Workflow**: Search Interface → LLM Processing → RAG Embedding → Similarity Search → LLM Generation → Result Display
   - **Job Seeker Workflow**: Profile Input → Data Processing → Preprocessing → Vector Storage

5. **Development Priorities Section Established**
   - **Immediate Tasks**: .env file setup, Swagger implementation, DB connection, Docker setup, JWT authentication
   - **Core Implementation**: Role-based access control, RAG system, OCR processing, Vector DB, AWS deployment

6. **Configuration File Guide Expanded**
   - Added environment variable (.env) setup guide
   - Included AWS and Docker deployment configuration
   - Detailed PostgreSQL and Redis connection settings

### Applied Additional Requirements
- Swagger를 통한 API 명세 관리
- 환경변수 .env 파일 생성 가이드
- PostgreSQL + Redis 데이터베이스 구성
- AWS + Docker 인프라 구성
- 사용자 역할별 완전한 기능 분리
- LLM과 RAG 기반 검색 플로우 상세 설명

## Current Implementation Details

### Domain Layer Structure
```
src/main/java/com/example/experfolio/domain/user/
├── entity/
│   ├── User.java                 # 사용자 기본 정보 (UUID, 인증, 상태)
│   ├── UserRole.java            # 역할 Enum (JOB_SEEKER, RECRUITER)
│   ├── UserStatus.java          # 상태 Enum (ACTIVE, INACTIVE, PENDING, SUSPENDED)
│   ├── JobSeekerProfile.java    # 구직자 상세 프로필
│   └── RecruiterProfile.java    # 채용담당자 상세 프로필
├── repository/
│   ├── UserRepository.java              # 사용자 데이터 접근 (한국어 주석)
│   ├── JobSeekerProfileRepository.java  # 구직자 프로필 쿼리 (RAG 검색 지원)
│   └── RecruiterProfileRepository.java  # 채용담당자 프로필 관리
├── service/
│   ├── UserService.java         # 사용자 관리 인터페이스
│   ├── UserServiceImpl.java     # 사용자 관리 구현체 (암호화, 인증)
│   ├── JobSeekerProfileService.java   # 구직자 프로필 서비스
│   ├── RecruiterProfileService.java   # 채용담당자 프로필 서비스
│   └── AuthService.java         # 인증/인가 서비스 (JWT, 로그인)
└── user.ddl                     # PostgreSQL DDL 스키마
```

### Security Layer Implementation
```
src/main/java/com/example/experfolio/global/security/
├── SecurityConfig.java          # Spring Security 메인 설정
└── jwt/
    ├── JwtTokenProvider.java     # JWT 토큰 생성/검증 (JJWT 0.12.6)
    ├── JwtTokenInfo.java         # 토큰 정보 DTO
    ├── JwtAuthenticationFilter.java      # JWT 인증 필터
    ├── JwtAuthenticationEntryPoint.java  # 401 에러 핸들러
    └── JwtAccessDeniedHandler.java       # 403 에러 핸들러
```

### Key Features Implemented

#### User Management System
- **회원가입/로그인**: 이메일 기반 JWT 인증
- **역할 관리**: JOB_SEEKER, RECRUITER 완전 분리
- **상태 관리**: ACTIVE, INACTIVE, PENDING, SUSPENDED
- **이메일 인증**: 토큰 기반 이메일 확인
- **비밀번호 관리**: BCrypt 암호화, 재설정 기능
- **소프트 삭제**: 데이터 보존과 함께 논리적 삭제

#### Profile Management System
- **구직자 프로필**: 개인정보, 희망조건, 포트폴리오 URL
- **채용담당자 프로필**: 회사정보, 부서, 인증 시스템
- **회사 인증**: 사업자등록번호, 인증 문서 관리
- **검색 최적화**: RAG 시스템을 위한 키워드 검색 지원

#### Security Features
- **JWT 이중 토큰**: Access Token (30분) + Refresh Token (7일)
- **역할 기반 접근 제어**: API 레벨 권한 분리
- **CORS 설정**: 개발/프로덕션 환경 대응
- **보안 헤더**: XSS, CSRF 방어
- **예외 처리**: 401/403 JSON 응답

#### Configuration Management
- **Multi-Profile**: dev/prod/test 환경별 설정
- **Environment Variables**: .env 파일 기반 설정
- **Database**: PostgreSQL 메인 + Redis 캐싱
- **Security Updates**: CVE 취약점 해결 (tess4j, commons-io)

### Database Schema
- **users**: 사용자 기본 정보 (UUID, 역할, 상태, 인증)
- **job_seeker_profiles**: 구직자 상세 프로필
- **job_seeker_urls**: 구직자 포트폴리오 URL 목록
- **recruiter_profiles**: 채용담당자 및 회사 정보
- **Indexes**: 성능 최적화를 위한 복합 인덱스
- **Triggers**: updated_at 자동 업데이트

### Development Environment
- **IDE**: IntelliJ IDEA 지원 (.idea 디렉토리)
- **Git**: 구조화된 커밋 메시지, .gitignore 최적화
- **Logging**: SLF4J + 환경별 로그 레벨 설정
- **Testing**: JUnit 5 + Spring Boot Test 환경

### Next Development Phase
**Phase 3: Controller Layer (예정)**
- REST API 엔드포인트 구현
- Request/Response DTO 설계
- Swagger API 문서화 완성
- 통합 테스트 구현