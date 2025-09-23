# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Experfolio is a Spring Boot 3.5.5 application built with Java 21 and Gradle. AI-powered portfolio scanning and search platform with JWT authentication. The application serves as a comprehensive job portfolio management and recruitment matching service, utilizing advanced AI technologies including RAG (Retrieval-Augmented Generation) and vector similarity search with a hybrid database architecture.

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
- **Portfolio Management**: Personal info, education, experience, tech stack, certifications, projects, cover letter
- **File Upload**: PDF upload with OCR text extraction, image upload
- **Profile Creation**: Comprehensive job seeker profiles stored in MongoDB

### Recruiter Features
- **AI-powered Search**: Natural language search capabilities with MongoDB text search
- **RAG Technology**: Retrieval-Augmented Generation for semantic search with scoring system
- **Advanced Filtering**: Filter by tech stack, experience, position using MongoDB aggregation
- **Profile Viewing**: Limited access to job seeker information (privacy protection)
- **Contact Feature**: Email contact functionality

### AI Technologies
- **Vector Similarity Search**: MongoDB Atlas Vector Search or separate vector database
- **OCR Processing**: Automatic text extraction from uploaded documents
- **Natural Language Processing**: For search queries and content analysis
- **MongoDB Text Search**: Full-text search capabilities for portfolio content

## Architecture and Structure

### Expected Package Structure
- **Base package**: `com.example.experfolio`
- **Controllers**: REST API endpoints for web interface
- **Services**: Business logic for user management, search, AI processing
- **Repositories**:
    - **JPA Repositories**: PostgreSQL (사용자, 인증, 회사 정보)
    - **MongoDB Repositories**: 포트폴리오, 검색 데이터, 벡터 임베딩
- **Models/Entities**:
    - **JPA Entities**: User, Company 관계형 데이터
    - **MongoDB Documents**: Portfolio, PersonalInfo, Education, Experience
- **Security**: JWT authentication and authorization
- **AI/Search**: RAG implementation, vector search, MongoDB text search
- **File Processing**: OCR and file handling
- **Configuration**:
    - **Database Config**: PostgreSQL + MongoDB dual database 설정
    - **MongoDB Config**: Connection, indexing, search configuration

### Technology Stack

#### Backend Framework
- **Spring Boot**: Main application framework
- **Spring Data JPA**: PostgreSQL database operations and ORM
- **Spring Data MongoDB**: MongoDB document operations and queries
- **Spring Security**: Authentication and authorization

#### Database

- **PostgreSQL**: 사용자 관리, 인증, 기본 메타데이터, 회사 정보
- **MongoDB**: 포트폴리오 데이터, 검색 인덱싱, AI/RAG 벡터 데이터
- **Redis**: 캐싱, 세션 관리, 성능 최적화

#### Infrastructure
- **AWS**: Cloud infrastructure and services
- **Docker**: Containerization for deployment
- **MongoDB Atlas**: Cloud MongoDB service (optional)

#### Key Dependencies (Expected)
- **Spring Boot Web**: REST API development
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: PostgreSQL database operations
- **Spring Data MongoDB**: NoSQL 데이터 접근
- **MongoDB Driver**: Java MongoDB 연결
- **JWT**: Token-based authentication
- **AI/ML Libraries**: For RAG and vector search implementation
- **OCR Libraries**: For document text extraction
- **File Processing**: PDF and image handling
- **Swagger/OpenAPI**: API documentation

### Configuration
- **Application properties**: `src/main/resources/application.properties`
- **Environment variables**: `.env` file for local development settings
- **Database configuration**:
    - **PostgreSQL**: 사용자 관리 및 관계형 데이터
    - **MongoDB**: 포트폴리오 문서 저장 및 검색
    - **Redis**: 캐싱 및 세션
- **MongoDB Indexes**:
    - Text search indexes for portfolio search
    - Compound indexes for filtering (tech stack, experience, location)
    - Geospatial indexes for location-based search
    - Vector indexes for AI similarity search
- **AI service configuration**: Settings for RAG and vector search
- **File upload configuration**: Storage and processing settings
- **Security configuration**: JWT and role-based access settings
- **AWS configuration**: Cloud services and Docker deployment settings

## MongoDB Integration
### Document Structure Design
- **Embedded Documents**: PersonalInfo, Education, Experience를 Portfolio에 임베드
- **Reference Documents**: User와 Portfolio 간 참조 관계 유지
- **Flexible Schema**: 다양한 포트폴리오 형태와 구조 지원
- **Nested Arrays**: 다중 학력, 경험, 자격증 정보 저장

### Search Optimization
- **Text Indexes**: 포트폴리오 내용 전문 검색 (한국어/영어 지원)
- **Compound Indexes**: 기술스택, 경험 수준, 위치 기반 복합 필터링
- **Aggregation Pipeline**: 복합 검색 쿼리, 통계, 그룹화
- **Performance Indexing**: 자주 검색되는 필드에 대한 최적화된 인덱스

### AI/RAG Integration
- **Vector Storage**: 포트폴리오 임베딩 벡터를 MongoDB에 저장
- **Semantic Search**: MongoDB Atlas Vector Search 활용 가능
- **Metadata Indexing**: AI 처리를 위한 메타데이터 및 태그 인덱싱
- **Hybrid Search**: 텍스트 검색과 벡터 유사도 검색 결합

### Data Migration Strategy
- **Gradual Migration**: PostgreSQL에서 MongoDB로 단계적 데이터 이관
- **Dual Write**: 마이그레이션 기간 중 양쪽 DB에 동시 쓰기
- **Validation Tools**: 데이터 일관성 검증 도구
- **Rollback Plan**: 필요시 PostgreSQL로 되돌리기 계획

## Development Environment

### IDE Support
- **IntelliJ IDEA**: Project is configured with .idea directory
- **Multiple IDE support**: .gitignore includes STS, IntelliJ, NetBeans, and VS Code
- **MongoDB Compass**: MongoDB GUI tool for development and debugging

### Testing
- **Framework**: JUnit 5 (JUnit Platform)
- **Test runner**: `./gradlew test`
- **Embedded MongoDB**: For integration testing
- **Testcontainers**: Docker-based testing for MongoDB and PostgreSQL

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
- **MongoDB Security**: Field-level encryption for sensitive portfolio data

### Search and Matching
- **Natural Language Queries**: Support conversational search inputs
- **Semantic Search**: RAG-based matching beyond keyword matching
- **MongoDB Text Search**: Full-text search with relevance scoring
- **Hybrid Search Strategy**: Combine text search, filters, and vector similarity
- **Multi-criteria Filtering**: Tech stack, experience level, position type using MongoDB aggregation

## Application Flow

### Recruiter Workflow (Updated with MongoDB)
1. **Search Interface**: Enter natural language search prompts
2. **Query Processing**: Parse and understand search intent
3. **MongoDB Search Strategy**:
    - Text search for keyword matching
    - Aggregation pipeline for complex filtering
    - Vector similarity for semantic matching
4. **Result Aggregation**: Combine and rank results from multiple search methods
5. **LLM Enhancement**: Use retrieved portfolio data for context-aware responses
6. **Results Display**: Present ranked candidate matches with relevance scores

### Job Seeker Workflow (Updated with MongoDB)
1. **Profile Input**: Enter personal and professional information
2. **Data Validation**: Validate and structure portfolio data
3. **Document Creation**: Convert data to MongoDB document format
4. **Document Storage**: Store in MongoDB with flexible schema
5. **Search Indexing**: Create text and vector indexes automatically
6. **RAG Integration**: Generate and store vector embeddings for semantic search

## Project Status

**Current Implementation Status (Phase 3 MongoDB Migration):**
- ✅ **Basic Spring Boot setup** completed
- ✅ **Domain Layer** implemented (User, JobSeekerProfile, RecruiterProfile entities)
- ✅ **PostgreSQL Repository Layer** completed with custom queries and Korean comments
- ✅ **Service Layer** fully implemented with comprehensive business logic
- ✅ **Security Layer** configured with JWT authentication and authorization
- ✅ **Controller Layer** implemented with comprehensive REST API endpoints
- ✅ **PostgreSQL Database Schema** designed and DDL generated
- ✅ **Configuration Management** with profile-based settings (dev/prod/test)
- ✅ **Exception Handling** global exception handler with custom exceptions
- ✅ **API Documentation** Swagger/OpenAPI fully configured
- ✅ **Testing Infrastructure** unit and integration tests implemented
- ✅ **Dependency Security** updated (resolved CVE vulnerabilities)
- ✅ **Git Repository** established with structured commits

**Technology Stack Implementation Status:**
- ✅ Spring Boot 3.3.5 + Java 21 + Gradle
- ✅ Spring Data JPA + PostgreSQL + Redis
- ✅ Spring Security + JWT (JJWT 0.12.6)
- ✅ Swagger/OpenAPI documentation complete
- ✅ Docker containerization configured
- ✅ Environment-based configuration (.env + profiles)
- ✅ File Processing libraries (Commons FileUpload, PDFBox, Tess4j OCR)
- ✅ Comprehensive validation and error handling

**MongoDB Integration Status:**
- 🔄 **Spring Data MongoDB** configuration in progress
- 🔄 **Document Modeling** Portfolio document structure design
- 🔄 **Dual Database Setup** PostgreSQL + MongoDB configuration
- 🔄 **Search Indexing** MongoDB text and compound indexes
- 🔄 **Migration Strategy** planning and implementation

**Next Phase Implementation:**

- 🔄 Portfolio Management System with MongoDB (Phase 3.2 in progress)
- 🔄 AI/RAG system integration with MongoDB vector storage
- 🔄 OCR processing implementation
- 🔄 MongoDB text search and aggregation
- 🔄 Frontend development

## Development Priorities

### ✅ Completed Tasks
- ✅ Environment configuration with `.env` file
- ✅ Swagger/OpenAPI configuration
- ✅ PostgreSQL and Redis connection setup
- ✅ Docker containerization setup
- ✅ JWT authentication system implementation
- ✅ User management with role-based access control
- ✅ Complete domain modeling and PostgreSQL database schema
- ✅ Comprehensive service layer with business logic
- ✅ Security configuration with CORS and exception handling
- ✅ **Controller Layer**: REST API endpoints implemented
- ✅ **Validation Layer**: Request/Response DTOs with validation
- ✅ **Exception Handling**: Global error handling and custom exceptions
- ✅ **Testing Infrastructure**: Unit and integration tests
- ✅ **API Documentation**: Complete Swagger documentation

### 🔄 Current Implementation Phase (Phase 3.2 - MongoDB Migration)

- **MongoDB Setup**: Spring Data MongoDB 설정 및 연결 구성
- **Document Modeling**: Portfolio, PersonalInfo, Education, Experience 문서 구조 설계
- **Dual Database Configuration**: PostgreSQL + MongoDB 하이브리드 설정
- **Portfolio Repository Layer**: MongoDB Repository 및 복합 쿼리 구현
- **Search Optimization**: MongoDB Text Search, Aggregation Pipeline, 인덱싱
- **Data Migration Tools**: PostgreSQL에서 MongoDB로 데이터 이관 도구 개발
- **Vector Storage**: AI 임베딩을 위한 MongoDB 벡터 필드 설계

### 🔄 Core AI Implementation (Phase 4)

- **RAG system integration** with MongoDB document retrieval
- **MongoDB Vector Search** integration for semantic similarity
- **OCR processing** for document handling with MongoDB storage
- **Hybrid Search Strategy** combining text search and vector similarity
- **File upload and processing** system with MongoDB GridFS
- **AWS deployment** configuration with MongoDB Atlas

## Additional Update Requirements

- Swagger를 통한 API 명세
- 환경 변수 .env 파일 생성
- MongoDB 연결 및 인덱싱 설정
- 하이브리드 데이터베이스 구성 관리

## Constraints Overview (Brief)

### User-specific Features & Permission Management
- 구직자(학생), 인사담당자(채용담당자) 사용자별 기능을 완전 분리하여 관리
- 구직자는 기업 사용자의 기능과 권한을 사용할 수 없음

### Backend Technology Stack

- Spring Boot
- JPA (PostgreSQL)
- Spring Data MongoDB
- AWS + Docker

### Database Architecture

- **PostgreSQL**: 사용자 관리, 인증, 회사 정보
- **MongoDB**: 포트폴리오 데이터, 검색, AI 임베딩
- **Redis**: 캐싱, 세션

## Detailed Expected Flow

### Recruiter (채용담당자) - Updated with MongoDB

1. 검색화면 - 프롬프트 입력
2. LLM - 프롬프트 입력 받고 의도 파악
3. MongoDB Search - 텍스트 검색 + 집계 파이프라인
4. RAG (리트리버) - MongoDB에서 관련 포트폴리오 문서 검색
5. Vector Search - 의미적 유사도 검색 (MongoDB Atlas Vector Search)
6. LLM - 프롬프트 + MongoDB 검색 결과 조합
7. 검색 결과 화면 - 랭킹된 후보자 목록과 관련성 점수

### Job Seeker (구직자/학생) - Updated with MongoDB

1. 정보 입력 페이지 - 포트폴리오 자료 입력
2. 백엔드 - 데이터 검증 및 구조화
3. MongoDB - 유연한 스키마로 포트폴리오 문서 저장
4. 인덱싱 - 텍스트 검색 및 벡터 인덱스 자동 생성
5. RAG - 포트폴리오 내용 임베딩 및 벡터 저장
6. 검색 최적화 - 향후 검색을 위한 메타데이터 태깅

## Current Implementation Details

### Domain Layer Structure (Updated with MongoDB)

```
src/main/java/com/example/experfolio/domain/
├── user/                         # 사용자 관리 도메인 (PostgreSQL 기반 - 완료)
│   ├── entity/
│   │   ├── User.java                 # 사용자 기본 정보 (UUID, 인증, 상태)
│   │   ├── UserRole.java            # 역할 Enum (JOB_SEEKER, RECRUITER)
│   │   ├── UserStatus.java          # 상태 Enum (ACTIVE, INACTIVE, PENDING, SUSPENDED)
│   │   ├── JobSeekerProfile.java    # 구직자 상세 프로필
│   │   └── RecruiterProfile.java    # 채용담당자 상세 프로필
│   ├── repository/
│   │   ├── UserRepository.java              # 사용자 데이터 접근 (한국어 주석)
│   │   ├── JobSeekerProfileRepository.java  # 구직자 프로필 쿼리
│   │   └── RecruiterProfileRepository.java  # 채용담당자 프로필 관리
│   ├── service/ [기존과 동일]
│   ├── controller/ [기존과 동일]
│   ├── dto/ [기존과 동일]
│   └── user.ddl                 # PostgreSQL DDL 스키마
├── portfolio/                    # 포트폴리오 관리 도메인 (MongoDB 기반 - 구현중)
│   ├── controller/              # 포트폴리오 REST API 
│   │   └── PortfolioController.java     # 포트폴리오 CRUD 및 검색 API
│   ├── dto/                     # 포트폴리오 Request/Response DTOs
│   │   ├── PortfolioCreateDto.java      # 포트폴리오 생성 요청
│   │   ├── PortfolioUpdateDto.java      # 포트폴리오 수정 요청
│   │   ├── PortfolioResponseDto.java    # 포트폴리오 응답
│   │   └── PortfolioSearchDto.java      # 검색 결과 DTO
│   ├── document/                # MongoDB Documents (JPA Entity 대신)
│   │   ├── Portfolio.java           # 메인 포트폴리오 문서
│   │   ├── PersonalInfo.java        # 개인정보 임베디드 문서
│   │   ├── Education.java           # 학력 임베디드 문서
│   │   ├── Experience.java          # 경험 임베디드 문서
│   │   ├── TechStack.java           # 기술스택 임베디드 문서
│   │   ├── Certification.java       # 자격증 임베디드 문서
│   │   ├── Project.java             # 프로젝트 임베디드 문서
│   │   └── VectorEmbedding.java     # AI 벡터 임베딩 데이터
│   ├── repository/              # MongoDB Repository
│   │   ├── PortfolioRepository.java     # Spring Data MongoDB 기반 CRUD
│   │   └── PortfolioSearchRepository.java # 복합 검색 쿼리 (Aggregation)
│   └── service/                 # 포트폴리오 비즈니스 로직
│       ├── PortfolioService.java        # 포트폴리오 CRUD 서비스
│       ├── PortfolioSearchService.java  # MongoDB 텍스트/집계 검색 서비스
│       └── PortfolioMigrationService.java # PostgreSQL → MongoDB 마이그레이션
├── search/                      # AI 검색 도메인 (MongoDB + Vector DB)
│   ├── service/
│   │   ├── RAGSearchService.java        # RAG 기반 의미적 검색
│   │   ├── VectorSearchService.java     # 벡터 유사도 검색
│   │   └── HybridSearchService.java     # 텍스트 + 벡터 하이브리드 검색
│   └── repository/
│       └── VectorEmbeddingRepository.java # 벡터 데이터 MongoDB 저장소
└── company/                     # 회사 정보 도메인 (PostgreSQL 기반)
```

### Global Infrastructure Layer (MongoDB 설정 추가)

```
src/main/java/com/example/experfolio/global/
├── config/                      # 설정 관리 (확장됨)
│   ├── SwaggerConfig.java            # Swagger/OpenAPI 설정
│   ├── EnvironmentConfig.java        # 환경 변수 설정
│   ├── DatabaseConfig.java           # PostgreSQL 설정
│   ├── MongoConfig.java              # MongoDB 연결 및 인덱스 설정
│   └── RedisConfig.java              # Redis 캐시 설정
├── security/ [기존과 동일]
├── exception/ [기존과 동일]
├── util/ [기존과 동일]
└── controller/ [기존과 동일]
```

### MongoDB Document Examples

#### Portfolio Document Structure

json

```json
{
  "_id": ObjectId("..."),
  "userId": "user-uuid-reference",
  "personalInfo": {
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678",
    "address": {
      "city": "서울",
      "district": "강남구"
    }
  },
  "educations": [
    {
      "institution": "한국대학교",
      "degree": "학사",
      "major": "컴퓨터공학",
      "startDate": ISODate("2020-03-01"),
      "endDate": ISODate("2024-02-28"),
      "gpa": 3.8
    }
  ],
  "experiences": [
    {
      "company": "테크회사",
      "position": "프론트엔드 개발자",
      "description": "React 기반 웹 애플리케이션 개발",
      "techStack": ["React", "JavaScript", "TypeScript"],
      "startDate": ISODate("2024-03-01"),
      "endDate": null,
      "isCurrentJob": true
    }
  ],
  "techStacks": [
    {
      "category": "Frontend",
      "skills": ["React", "Vue.js", "Angular"],
      "proficiency": "Advanced"
    },
    {
      "category": "Backend", 
      "skills": ["Spring Boot", "Node.js"],
      "proficiency": "Intermediate"
    }
  ],
  "projects": [
    {
      "title": "포트폴리오 관리 시스템",
      "description": "Spring Boot와 MongoDB를 사용한 포트폴리오 플랫폼",
      "techStack": ["Spring Boot", "MongoDB", "React"],
      "githubUrl": "https://github.com/user/portfolio",
      "demoUrl": "https://portfolio-demo.com"
    }
  ],
  "certifications": [
    {
      "name": "정보처리기사",
      "issuer": "한국산업인력공단",
      "issueDate": ISODate("2023-11-15"),
      "expiryDate": null
    }
  ],
  "vectorEmbedding": {
    "textEmbedding": [0.1, 0.2, 0.3, ...], // 768차원 벡터
    "skillEmbedding": [0.4, 0.5, 0.6, ...],
    "lastUpdated": ISODate("2024-09-17")
  },
  "searchMetadata": {
    "keywords": ["React", "Spring Boot", "프론트엔드", "백엔드"],
    "experienceLevel": "Junior",
    "preferredPosition": "풀스택 개발자",
    "location": "서울"
  },
  "createdAt": ISODate("2024-09-17"),
  "updatedAt": ISODate("2024-09-17")
}
```

### Key Features Implemented & Planned

#### Completed (PostgreSQL 기반)

- **User Management System**: JWT 인증, 역할 관리
- **Profile Management**: 구직자/채용담당자 프로필
- **Security Features**: 이중 토큰, RBAC
- **API Documentation**: Swagger 완료

#### In Progress (MongoDB Migration)

- **Document Modeling**: Portfolio 문서 구조 설계
- **Search Indexing**: 텍스트 검색 및 복합 인덱스
- **Migration Tools**: PostgreSQL → MongoDB 이관 도구
- **Hybrid Database**: PostgreSQL + MongoDB 설정

#### Planned (AI Integration)

- **Vector Search**: MongoDB Atlas Vector Search 또는 별도 벡터 DB
- **RAG Integration**: 문서 검색 + LLM 생성
- **OCR Processing**: 파일 업로드 및 텍스트 추출
- **Semantic Search**: 자연어 검색 처리

### MongoDB Indexes Design

javascript

```javascript
// Text Search Index
db.portfolios.createIndex({
  "personalInfo.name": "text",
  "experiences.description": "text", 
  "projects.description": "text",
  "techStacks.skills": "text"
}, {
  weights: {
    "techStacks.skills": 10,
    "experiences.description": 5,
    "personalInfo.name": 3
  },
  name: "portfolio_text_search"
});

// Compound Index for Filtering
db.portfolios.createIndex({
  "techStacks.skills": 1,
  "searchMetadata.experienceLevel": 1,
  "searchMetadata.location": 1,
  "updatedAt": -1
});

// Vector Search Index (MongoDB Atlas)
db.portfolios.createSearchIndex({
  "name": "vector_search",
  "definition": {
    "fields": [{
      "type": "vector", 
      "path": "vectorEmbedding.textEmbedding",
      "numDimensions": 768,
      "similarity": "cosine"
    }]
  }
});
```