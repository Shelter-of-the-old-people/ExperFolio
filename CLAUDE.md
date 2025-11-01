# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Experfolio** is a Spring Boot 3.3.5 application built with Java 21 and Gradle. It's an AI-powered portfolio scanning and search platform with JWT authentication, designed as a comprehensive job portfolio management and recruitment matching service.

**Current Status**: 60% Complete - Phase 3.2 (MongoDB Integration)
**Git Branch**: `feature-ydg`
**Last Updated**: October 28, 2025

---

## Build System and Commands

### Gradle Commands
```bash
# Build the project
./gradlew build

# Build without tests (faster)
./gradlew build -x test

# Run the application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when application is running)
- **API Specification**: http://localhost:8080/api-docs

### Environment Configuration
- **Environment Variables**: `.env` file in project root (auto-loaded with spring-dotenv)
- **Spring Profiles**: `dev`, `test`, `prod` (configured in `src/main/resources/application*.yml`)

---

## Current Implementation Status

### ✅ COMPLETED FEATURES (100%)

#### 1. User Management & Authentication
- User registration with role selection (JOB_SEEKER | RECRUITER)
- Email-based login with JWT tokens
- Token refresh mechanism (separate access & refresh tokens)
- Password reset flow with secure tokens
- Soft delete for user accounts (deletedAt field)
- JobSeekerProfile and RecruiterProfile entities
- PostgreSQL integration with Spring Data JPA

**Recent Changes (Oct 20, 2025)**:
- ❌ Removed `UserStatus` enum completely
- ❌ Removed email verification system
- ✅ Simplified to soft-delete only (deletedAt IS NULL = active)

**API Endpoints**:
- `POST /api/v1/auth/signup` - Register new user
- `POST /api/v1/auth/login` - Login and get tokens
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout (client-side)
- `GET /api/v1/users/me` - Get current user info
- `PUT /api/v1/users/me` - Update user info

#### 2. Portfolio Management (80%)
- Create portfolio for job seekers
- Retrieve user's portfolio
- Update basic information (name, school, major, GPA, awards, certifications, languages)
- Add/update/delete portfolio items (projects, activities, research - max 5)
- Reorder portfolio items
- File upload and attachment handling (images, PDFs, docs)
- MongoDB document storage with embedded structure

**API Endpoints**:
- `POST /api/portfolios` - Create portfolio
- `GET /api/portfolios/me` - Get user's portfolio
- `PUT /api/portfolios/basic-info` - Update basic info
- `POST /api/portfolios/items` - Add portfolio item (with file upload)
- `PUT /api/portfolios/items/{itemId}` - Update item
- `DELETE /api/portfolios/items/{itemId}` - Delete item
- `PUT /api/portfolios/items/reorder` - Reorder items
- `DELETE /api/portfolios` - Delete entire portfolio

**File Storage**:
- Location: `uploads/{userId}/{filename}`
- Supported formats: jpg, jpeg, png, gif, pdf, doc, docx, txt
- Max file size: 10MB
- UUID-based filenames for uniqueness

### ❌ NOT IMPLEMENTED (0%)

#### 3. Search & Discovery System
- No SearchController, SearchService, or Repository
- MongoDB text search indexes not created
- Aggregation pipelines not implemented
- Natural language query processing missing
- Result ranking and relevance scoring missing

#### 4. AI/RAG Integration
- LLM integration (ChatGPT/Claude API) not implemented
- Vector embedding generation missing
- Vector storage and retrieval not implemented
- RAG pipeline not built
- Dependencies exist (tess4j, pdfbox) but not used

#### 5. Recruiter Features
- Candidate search interface not implemented
- Profile viewing (privacy-limited) missing
- Contact/messaging system not implemented
- Saved searches and favoriting missing

#### 6. OCR Processing
- Tess4j library included but not integrated
- PDF text extraction not implemented
- Text indexing for search missing

#### 7. Admin Features
- Admin dashboard not implemented
- User management UI missing
- Platform statistics not implemented

---

## Architecture and Structure

### Actual Domain Structure

```
src/main/java/com/example/experfolio/domain/
├── user/                           # User management (PostgreSQL) ✅ COMPLETE
│   ├── entity/
│   │   ├── User.java              # Base user entity (UUID, email, password, role)
│   │   ├── UserRole.java          # Enum: JOB_SEEKER, RECRUITER
│   │   ├── JobSeekerProfile.java  # Extended profile for job seekers
│   │   └── RecruiterProfile.java  # Extended profile for recruiters
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── JobSeekerProfileRepository.java
│   │   └── RecruiterProfileRepository.java
│   ├── service/
│   │   ├── UserService.java       # User CRUD operations
│   │   ├── UserServiceImpl.java
│   │   ├── AuthService.java       # Authentication logic
│   │   └── AuthServiceImpl.java
│   ├── controller/
│   │   ├── UserController.java    # /api/v1/users/**
│   │   └── AuthController.java    # /api/v1/auth/**
│   └── dto/
│       ├── SignupRequestDto.java
│       ├── LoginRequestDto.java
│       ├── UserResponseDto.java
│       └── ... (various DTOs)
│
├── portfolio/                      # Portfolio management (MongoDB) 80% COMPLETE
│   ├── controller/
│   │   └── PortfolioController.java  # /api/portfolios/**
│   ├── service/
│   │   ├── PortfolioService.java
│   │   ├── PortfolioServiceImpl.java
│   │   └── FileStorageService.java   # File upload/delete handling
│   ├── repository/
│   │   └── PortfolioRepository.java  # Spring Data MongoDB
│   ├── document/                     # MongoDB documents
│   │   ├── Portfolio.java            # Main portfolio document
│   │   ├── BasicInfo.java            # Embedded: name, school, GPA, etc.
│   │   ├── PortfolioItem.java        # Embedded: projects/activities
│   │   ├── Attachment.java           # Embedded: file attachments
│   │   ├── Award.java                # Embedded: awards
│   │   ├── Certification.java        # Embedded: certifications
│   │   ├── Language.java             # Embedded: language test scores
│   │   ├── Embeddings.java           # Placeholder for vector data (NOT USED)
│   │   └── ProcessingStatus.java     # Track if embedding needed
│   └── dto/
│       ├── PortfolioResponseDto.java
│       ├── BasicInfoDto.java
│       ├── PortfolioItemDto.java
│       └── ReorderRequestDto.java
│
├── search/                         # Search system ❌ EMPTY - placeholder only
│   └── (empty directories)
│
└── company/                        # Company info ❌ EMPTY - placeholder only
    └── (empty directories)
```

### Global Infrastructure

```
src/main/java/com/example/experfolio/global/
├── config/
│   ├── SwaggerConfig.java         # OpenAPI/Swagger configuration
│   ├── DatabaseConfig.java        # PostgreSQL configuration
│   ├── MongoConfig.java           # MongoDB configuration
│   └── RedisConfig.java           # Redis config (currently disabled)
├── security/
│   ├── SecurityConfig.java        # Spring Security configuration
│   ├── jwt/
│   │   ├── JwtTokenProvider.java  # JWT creation/validation
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtAuthenticationEntryPoint.java
│   └── CorsConfig.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── CustomException.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── UnauthorizedException.java
└── util/
    └── (various utilities)
```

---

## Technology Stack

### Implemented Dependencies

**Framework**:
- Spring Boot 3.3.5
- Java 21
- Gradle 8.x

**Databases**:
- PostgreSQL (Users, authentication, profiles)
- MongoDB (Portfolios, embedded documents)
- Redis (Configured but **DISABLED**)

**Key Libraries**:
- `spring-boot-starter-data-jpa` - PostgreSQL ORM
- `spring-boot-starter-data-mongodb` - MongoDB integration
- `spring-boot-starter-security` - Authentication/Authorization
- `jjwt` 0.12.6 - JWT token handling
- `springdoc-openapi-starter-webmvc-ui` 2.2.0 - Swagger/OpenAPI
- `commons-fileupload` 1.5 - File upload
- `pdfbox` 3.0.0 - PDF processing (not integrated yet)
- `tess4j` 5.16.0 - OCR (not integrated yet)
- `spring-dotenv` 4.0.0 - .env file support
- `lombok` - Code generation

**Note**: Redis starter is commented out in build.gradle

---

## Database Schema

### PostgreSQL Schema

**users** table:
```sql
id              UUID PRIMARY KEY
email           VARCHAR(255) UNIQUE NOT NULL
password        VARCHAR(255) NOT NULL
name            VARCHAR(100)
phone_number    VARCHAR(20)
role            VARCHAR(50) NOT NULL  -- JOB_SEEKER | RECRUITER
password_reset_token       VARCHAR(255)
password_reset_expires     TIMESTAMP
last_login_at   TIMESTAMP
created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
deleted_at      TIMESTAMP NULL  -- NULL = active, NOT NULL = soft deleted
```

**job_seeker_profiles** table:
```sql
id                  UUID PRIMARY KEY
user_id             UUID REFERENCES users(id)
first_name          VARCHAR(50)
last_name           VARCHAR(50)
phone               VARCHAR(20)
birth_date          DATE
gender              VARCHAR(10)
address             VARCHAR(255)
state               VARCHAR(100)
postal_code         VARCHAR(20)
profile_image_url   VARCHAR(500)
summary             TEXT
career_objective    TEXT
desired_position    VARCHAR(200)
desired_salary_min  INTEGER
desired_salary_max  INTEGER
desired_location    VARCHAR(200)
available_start_date DATE
portfolio_id        VARCHAR(100)  -- Reference to MongoDB
urls                TEXT[]
created_at          TIMESTAMP
updated_at          TIMESTAMP
```

**recruiter_profiles** table:
```sql
id                              UUID PRIMARY KEY
user_id                         UUID REFERENCES users(id)
first_name                      VARCHAR(50)
last_name                       VARCHAR(50)
phone                           VARCHAR(20)
company_name                    VARCHAR(200)
company_website                 VARCHAR(500)
company_description             TEXT
department                      VARCHAR(100)
position                        VARCHAR(100)
business_registration_number    VARCHAR(50)
is_company_verified             BOOLEAN DEFAULT FALSE
company_verification_document_url VARCHAR(500)
created_at                      TIMESTAMP
updated_at                      TIMESTAMP
```

### MongoDB Document Structure

**portfolios** collection:
```javascript
{
  _id: ObjectId("..."),
  userId: "uuid-string",  // FK to PostgreSQL users table

  basicInfo: {
    name: "홍길동",
    schoolName: "한국대학교",
    major: "컴퓨터공학",
    gpa: 3.8,
    desiredPosition: "백엔드 개발자",
    referenceUrl: ["https://github.com/user"],
    awards: [
      { awardName: "해커톤 대상", achievement: "1등", awardY: "2024" }
    ],
    certifications: [
      { certificationName: "정보처리기사", issueY: "2024" }
    ],
    languages: [
      { testName: "TOEIC", score: "900", issueY: "2024" }
    ]
  },

  portfolioItems: [
    {
      id: "uuid-string",
      order: 1,
      type: "project",  // project | activity | research | other
      title: "포트폴리오 관리 시스템",
      content: "Spring Boot와 MongoDB를 사용한...",
      attachments: [
        {
          filePath: "uploads/user-id/uuid-filename.pdf",
          extractionStatus: "pending"  // pending | completed | failed
        }
      ],
      createdAt: ISODate("2024-10-28"),
      updatedAt: ISODate("2024-10-28")
    }
  ],

  embeddings: {
    searchableText: "...",
    kureVector: [],  // NOT GENERATED YET - placeholder
    lastUpdated: ISODate("2024-10-28")
  },

  processingStatus: {
    needsEmbedding: true,
    lastProcessed: null
  },

  createdAt: ISODate("2024-10-28"),
  updatedAt: ISODate("2024-10-28")
}
```

**Important**:
- Vector embeddings are NOT currently generated
- OCR text extraction is NOT implemented
- Search indexes are NOT created yet

---

## Configuration & Environment

### Required Environment Variables (.env)

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=experfolio
DB_USERNAME=admin
DB_PASSWORD=your-password

# MongoDB
MONGODB_URI=mongodb://localhost:27017/experfolio
MONGODB_DATABASE=experfolio

# JWT
JWT_SECRET=your-secret-key-minimum-256-bits
JWT_ACCESS_TOKEN_VALIDITY=1800       # 30 minutes
JWT_REFRESH_TOKEN_VALIDITY=604800    # 7 days

# File Upload
FILE_UPLOAD_MAX_SIZE=10MB
FILE_UPLOAD_DIR=uploads

# Server
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

### Spring Profiles

- **dev**: Development mode with verbose logging, auto-update schema
- **test**: Testing environment with H2/embedded databases
- **prod**: Production settings with optimized performance

---

## Development Guidelines

### User Roles & Access Control

**JOB_SEEKER**:
- Can create and manage their own portfolio
- Can upload files and attachments
- Can update personal profile information
- **Cannot** access recruiter features

**RECRUITER**:
- Can manage company profile
- **Will be able** to search candidates (not implemented yet)
- **Will be able** to view limited candidate info (not implemented yet)
- **Cannot** access job seeker portfolio management

### Privacy & Security

- Passwords are BCrypt hashed
- JWT tokens for stateless authentication
- Soft delete for user accounts (preserves data integrity)
- File uploads are validated by type and size
- CORS configured for allowed origins
- No email verification (removed for simplicity)

### Portfolio Management Rules

- Job seekers can have exactly **1 portfolio**
- Each portfolio can have up to **5 portfolio items** (projects/activities/research)
- Each item can have multiple file attachments
- Files are stored in `uploads/{userId}/` directory
- File deletion cascades when item/portfolio is deleted

---

## API Documentation

### Authentication Endpoints

```
POST /api/v1/auth/signup
  Request: { email, password, name, role }
  Response: { user info, tokens }

POST /api/v1/auth/login
  Request: { email, password }
  Response: { accessToken, refreshToken }

POST /api/v1/auth/refresh
  Request: { refreshToken }
  Response: { new accessToken }

POST /api/v1/auth/logout
  Request: (client-side token deletion)
  Response: 200 OK
```

### Portfolio Endpoints

```
POST /api/portfolios
  Headers: Authorization: Bearer {token}
  Request: { basicInfo: {...} }
  Response: Portfolio object

GET /api/portfolios/me
  Headers: Authorization: Bearer {token}
  Response: Portfolio object or 404

PUT /api/portfolios/basic-info
  Headers: Authorization: Bearer {token}
  Request: { name, schoolName, major, gpa, ... }
  Response: Updated portfolio

POST /api/portfolios/items
  Headers: Authorization: Bearer {token}
  Content-Type: multipart/form-data
  Request: item={...json}, files[]
  Response: Updated portfolio

PUT /api/portfolios/items/{itemId}
  Headers: Authorization: Bearer {token}
  Content-Type: multipart/form-data
  Request: item={...json}, files[]
  Response: Updated portfolio

DELETE /api/portfolios/items/{itemId}
  Headers: Authorization: Bearer {token}
  Response: 204 No Content

PUT /api/portfolios/items/reorder
  Headers: Authorization: Bearer {token}
  Request: { items: [{id, order}] }
  Response: Updated portfolio
```

Full API documentation available at: http://localhost:8080/swagger-ui.html

---

## Known Issues & Technical Debt

### Critical Issues
1. **No search implementation** - Search domain is completely empty
2. **No vector embeddings** - Embeddings field exists but never populated
3. **No OCR integration** - Libraries included but not used
4. **Cross-database transactions** - Portfolio operations span PostgreSQL + MongoDB without true ACID

### Medium Issues
1. **Inconsistent API versioning** - `/api/v1/auth` vs `/api/portfolios`
2. **Redis disabled** - Dependency exists but not in use
3. **No email service** - Password reset emails not actually sent
4. **File cleanup job missing** - Orphaned files not automatically deleted

### Minor Issues
1. **Hardcoded error messages** - Should use i18n or constants
2. **No request logging** - Could improve debugging
3. **No rate limiting** - Should add for production

---

## Development Roadmap

### Next Priority: Search Implementation (Phase 4)
**Estimated: 2-3 weeks**
- [ ] Create SearchController and SearchService
- [ ] Implement MongoDB text search indexes
- [ ] Build aggregation pipeline for filtering
- [ ] Implement result ranking algorithm
- [ ] Add pagination support

### Future: AI/RAG Integration (Phase 5)
**Estimated: 3-4 weeks**
- [ ] Integrate LLM API (OpenAI or Claude)
- [ ] Implement embedding generation service
- [ ] Set up vector storage in MongoDB
- [ ] Build RAG retrieval pipeline
- [ ] Implement semantic search

### Future: OCR Processing (Phase 6)
**Estimated: 2 weeks**
- [ ] Integrate Tess4j for OCR
- [ ] Extract text from PDFs with PDFBox
- [ ] Store extracted text in embeddings.searchableText
- [ ] Index extracted text for search

### Future: Recruiter Features (Phase 7)
**Estimated: 2-3 weeks**
- [ ] Build recruiter search interface
- [ ] Implement candidate filtering
- [ ] Add privacy controls for job seeker data
- [ ] Build contact/messaging system

---

## Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceImplTest

# Run with coverage
./gradlew test jacocoTestReport
```

**Note**: Some tests may fail due to removed features (UserStatus, email verification). These tests need to be updated.

---

## Deployment

### Docker Build
```bash
# Build Docker image
docker build -t experfolio:latest .

# Run container
docker run -p 8080:8080 --env-file .env experfolio:latest
```

### Database Migrations
- Flyway configured for PostgreSQL migrations
- Migration files in `src/main/resources/db/migration/`
- Migrations run automatically on application startup

---

## Important Notes for Claude Code

### When Working on This Project:

1. **UserStatus and email verification are REMOVED** - Do not reference these in code
2. **Redis is DISABLED** - Do not try to use Redis
3. **Search domain is EMPTY** - Treat it as a future feature to implement
4. **Vector embeddings are NOT generated** - Embeddings field exists but is never populated
5. **OCR is NOT integrated** - Tess4j and PDFBox are dependencies but not used yet
6. **MongoDB documents use embedded structure** - Not separate collections for awards/certifications/etc.
7. **Portfolio items are limited to 5** - This is a business rule enforced in the service layer
8. **File uploads go to local `uploads/` directory** - Not S3 or cloud storage yet
9. **Active users are identified by `deletedAt IS NULL`** - No status field exists

### Current Git Status:
- Branch: `feature-ydg`
- Latest commit: "refactor: Remove UserStatus enum and email verification feature"
- Modified files (unstaged): build.gradle, some DTOs, repositories

### Development Environment:
- Java 21 (Amazon Corretto recommended)
- PostgreSQL 14+
- MongoDB 5.0+
- Gradle 8.x
- Port 8080 for the application

---

**Last Updated**: October 28, 2025
**Maintainer**: Experfolio Team
**Status**: 60% Complete - Active Development