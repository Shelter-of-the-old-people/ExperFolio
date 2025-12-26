# Experfolio

경험 기반 포트폴리오 관리 플랫폼

## 프로젝트 개요

Experfolio는 사용자의 경험과 포트폴리오를 효과적으로 관리하고 공유할 수 있는 플랫폼입니다. OCR 기술과 AI를 활용하여 문서에서 자동으로 정보를 추출하고, 벡터 데이터베이스를 통해 지능형 검색 및 추천 기능을 제공합니다.

## 주요 기능

- **포트폴리오 관리**: 개인 포트폴리오 생성, 수정, 조회
- **사용자 인증**: JWT 기반 보안 인증 시스템
- **문서 처리**: OCR 및 PDF 처리를 통한 자동 정보 추출
- **지능형 검색**: 벡터 데이터베이스(ChromaDB)를 활용한 시맨틱 검색
- **즐겨찾기**: 포트폴리오 북마크 기능
- **파일 관리**: Cloudflare R2 기반 클라우드 스토리지
- **AI 통합**: OpenAI 및 Claude API 지원

## 기술 스택

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security + JWT
- **API Documentation**: Swagger/OpenAPI

### Database
- **PostgreSQL**: 주 관계형 데이터베이스
- **MongoDB**: 문서 저장소
- **Redis**: 캐싱 레이어
- **ChromaDB**: 벡터 데이터베이스 (AI 검색)

### Libraries & Tools
- **OCR**: Tesseract (Tess4j)
- **PDF Processing**: Apache PDFBox
- **File Storage**: AWS S3 SDK (Cloudflare R2 호환)
- **Build Tool**: Gradle
- **Containerization**: Docker, Docker Compose

## 시작하기

### 필요 조건

- Java 21 이상
- Docker & Docker Compose
- Gradle (또는 포함된 Gradle Wrapper 사용)

### 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 변수들을 설정하세요:

```env
# Database
DB_NAME=experfolio
DB_USERNAME=experfolio_user
DB_PASSWORD=your_password
DB_PORT=5432

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# AI APIs
OPENAI_API_KEY=your_openai_api_key
CLAUDE_API_KEY=your_claude_api_key

# Cloudflare R2
R2_ACCESS_KEY=your_r2_access_key
R2_SECRET_KEY=your_r2_secret_key
R2_BUCKET_NAME=your_bucket_name
R2_ENDPOINT=your_r2_endpoint

# Server
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

### Docker로 실행

1. 모든 서비스를 Docker Compose로 시작:
```bash
docker-compose up -d
```

2. 애플리케이션 로그 확인:
```bash
docker-compose logs -f app
```

3. 서비스 종료:
```bash
docker-compose down
```

### 로컬 개발 환경에서 실행

1. 데이터베이스 서비스만 실행:
```bash
docker-compose up -d postgres redis chromadb
```

2. 애플리케이션 빌드 및 실행:
```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/Experfolio-0.0.1-SNAPSHOT.jar
```

### 테스트 실행

```bash
./gradlew test
```

## API 문서

애플리케이션 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

## 프로젝트 구조

```
src/
├── main/
│   └── java/com/example/experfolio/
│       ├── domain/
│       │   ├── portfolio/      # 포트폴리오 도메인
│       │   ├── user/           # 사용자 및 인증
│       │   ├── favorite/       # 즐겨찾기
│       │   └── search/         # 검색
│       ├── global/             # 공통 기능 및 설정
│       └── infrastructure/     # 외부 서비스 연동
└── test/                       # 테스트 코드
```

## 데이터베이스 마이그레이션

프로젝트에는 다음 SQL 마이그레이션 스크립트가 포함되어 있습니다:

- `postgresql_migration.sql`: PostgreSQL 초기 스키마
- `complete_user_cleanup_migration.sql`: 사용자 데이터 정리
- `remove_user_status_migration.sql`: 사용자 상태 필드 제거

## 헬스 체크

애플리케이션 상태 확인:

```bash
curl http://localhost:8080/actuator/health
```

## 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 라이선스

이 프로젝트는 캡스톤 프로젝트로 개발되었습니다.

## 문의

프로젝트 관련 문의사항은 이슈를 생성해주세요.
