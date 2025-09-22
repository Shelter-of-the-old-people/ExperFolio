# 추가적으로 업데이트할 사항

- Swagger를 통한 API 명세
- 환경 변수 .env 파일 생성
- MongoDB 연결 및 인덱싱 설정
- 하이브리드 데이터베이스 구성 관리

## 제약사항 소개 (간략)

### 사용자별 기능 & 권한 관리
- 구직자(학생), 인사담당자(채용담당자) 사용자별 기능을 완전 분리하여 관리
- 구직자는 기업 사용자의 기능과 권한을 사용할 수 없음

### 백엔드 기술스택

- Spring Boot
- JPA (PostgreSQL)
- Spring Data MongoDB
- AWS + Docker

## 데이터베이스
- **PostgreSQL**: 사용자 관리, 인증, 회사 정보
- **MongoDB**: 포트폴리오 데이터, 검색, AI 임베딩
- **Redis**: 캐싱, 세션

---

## 간략한 예상 flow

### 채용담당자 (MongoDB 기반 업데이트)
1. 검색화면 - 프롬프트 입력
2. LLM - 프롬프트 입력 받고 의도 파악
3. MongoDB Search - 텍스트 검색 + 집계 파이프라인
4. RAG (리트리버) - MongoDB에서 관련 포트폴리오 문서 검색
5. Vector Search - 의미적 유사도 검색 (MongoDB Atlas Vector Search)
6. LLM - 프롬프트 + MongoDB 검색 결과 조합
7. 검색 결과 화면 - 랭킹된 후보자 목록과 관련성 점수

### 구직자(학생) (MongoDB 기반 업데이트)
1. 정보 입력 페이지 - 포트폴리오 자료 입력
2. 백엔드 - 데이터 검증 및 구조화
3. MongoDB - 유연한 스키마로 포트폴리오 문서 저장
4. 인덱싱 - 텍스트 검색 및 벡터 인덱스 자동 생성
5. RAG - 포트폴리오 내용 임베딩 및 벡터 저장
6. 검색 최적화 - 향후 검색을 위한 메타데이터 태깅

--- 

# 수정사항 반영 내역

## CLAUDE.md 파일 3차 업데이트 완료 (MongoDB 하이브리드 아키텍처 반영)

### 주요 변경 사항

1. **하이브리드 데이터베이스 아키텍처 도입**
   - **PostgreSQL**: 사용자 관리, 인증, 회사 정보 (관계형 데이터)
   - **MongoDB**: 포트폴리오 데이터, 검색 인덱싱, AI/RAG 벡터 데이터
   - **Redis**: 캐싱, 세션 관리, 성능 최적화
   - Spring Data JPA + Spring Data MongoDB 병행 사용

2. **MongoDB 통합 기술 스택**
   - Spring Data MongoDB: NoSQL 데이터 접근
   - MongoDB Atlas Vector Search: AI 유사도 검색
   - MongoDB Text Search: 전문 검색 엔진
   - MongoDB Aggregation Pipeline: 복합 검색 쿼리

3. **프로젝트 구조 업데이트**
   - **Domain 계층**: User (PostgreSQL) + Portfolio (MongoDB) 분리
   - **Document 모델링**: 유연한 스키마로 포트폴리오 구조 설계
   - **하이브리드 Repository**: JPA + MongoDB Repository 병행
   - **Migration Strategy**: PostgreSQL → MongoDB 단계적 이관

4. **AI/검색 시스템 고도화**
   - **MongoDB Vector Search**: 의미적 유사도 검색
   - **Text + Vector 하이브리드**: 키워드 + 벡터 결합 검색
   - **Aggregation Pipeline**: 복합 필터링 및 통계
   - **Index Optimization**: 검색 성능 최적화

5. **개발 단계 재구성**
   - **Phase 3.2**: MongoDB Migration (현재 진행 중)
   - **Document Structure**: Portfolio, PersonalInfo, Education, Experience
   - **Search Indexing**: 텍스트, 복합, 벡터 인덱스 설계
   - **Migration Tools**: 데이터 이관 도구 개발

6. **실제 구현 상태 반영**
   - ✅ PostgreSQL 기반 사용자 관리 시스템 완료
   - ✅ JWT 인증, Security, API 문서화 완료
   - 🔄 MongoDB 포트폴리오 시스템 구현 중
   - 🔄 하이브리드 검색 엔진 개발 중

### 반영된 추가 요구사항
- MongoDB 연결 및 인덱싱 설정
- 하이브리드 데이터베이스 구성 관리
- MongoDB Document 모델링 가이드
- Vector Search 통합 계획
- Dual Database Migration 전략

---

# 개발 계획 (MongoDB 하이브리드 아키텍처 기반)

## Phase 1: 프로젝트 기반 설정 및 환경 구축 ✅ 완료

### 1.1 개발 환경 설정 ✅
- [x] `.env` 파일 생성 및 환경변수 설정
- [x] `application.properties` 설정 (dev/prod/test 프로파일)
- [x] PostgreSQL 데이터베이스 연결 설정
- [x] Redis 캐시 서버 연결 설정
- [x] Docker 설정 파일 작성 (`Dockerfile`, `docker-compose.yml`)

### 1.2 기본 의존성 및 설정 ✅
- [x] `build.gradle`에 필요한 의존성 추가:
  - Spring Security, JWT (JJWT 0.12.6)
  - Spring Data JPA, PostgreSQL Driver
  - Redis, Swagger/OpenAPI
  - 파일 업로드 관련 라이브러리 (CVE 취약점 해결)
- [x] Swagger 설정 및 API 문서화 기본 구조
- [x] 기본 패키지 구조 생성

## Phase 2: 사용자 관리 및 인증 시스템 ✅ 완료

### 2.1 사용자 엔티티 및 데이터베이스 설계 ✅
- [x] User 엔티티 설계 (구직자/채용담당자 역할 구분)
- [x] PostgreSQL 데이터베이스 테이블 설계 및 DDL 생성
- [x] JPA Repository 구현 (한국어 주석)

### 2.2 JWT 인증 시스템 ✅
- [x] JWT 토큰 생성 및 검증 유틸리티 클래스
- [x] Spring Security 설정 (CORS, CSRF, 예외 처리)
- [x] 로그인/회원가입 API 구현
- [x] 역할별 접근 권한 설정 (Role-based Access Control)

### 2.3 사용자 관리 API ✅
- [x] 회원가입 API (구직자/채용담당자)
- [x] 로그인/로그아웃 API
- [x] 사용자 정보 조회/수정 API
- [x] 비밀번호 변경 API

## Phase 3: MongoDB 통합 포트폴리오 시스템 🔄 진행 중

### 3.1 MongoDB 설정 및 연결 🔄
- [ ] Spring Data MongoDB 의존성 추가 및 설정
- [ ] MongoDB 연결 설정 (로컬 + Atlas 클라우드)
- [ ] 하이브리드 데이터베이스 설정 (PostgreSQL + MongoDB)
- [ ] MongoDB 인덱스 설계 및 생성

### 3.2 포트폴리오 Document 모델링 🔄
- [ ] Portfolio Document 구조 설계 (메인 문서)
- [ ] PersonalInfo, Education, Experience 임베디드 문서
- [ ] TechStack, Certification, Project 임베디드 문서
- [ ] VectorEmbedding 필드 (AI 벡터 저장)

### 3.3 MongoDB Repository 구현 🔄
- [ ] PortfolioRepository (Spring Data MongoDB)
- [ ] PortfolioSearchRepository (Aggregation Pipeline)
- [ ] 복합 검색 쿼리 구현 (텍스트 + 필터)
- [ ] MongoDB GridFS (파일 저장용)

### 3.4 포트폴리오 CRUD API 🔄
- [ ] 포트폴리오 생성/수정/삭제 API (MongoDB 기반)
- [ ] 개인정보 관리 API

### 3.5 데이터 마이그레이션 도구 🔄
- [ ] PostgreSQL → MongoDB 마이그레이션 서비스
- [ ] 기존 데이터 변환 및 검증 도구
- [ ] Dual Write 시스템 (양쪽 DB 동시 쓰기)
- [ ] 데이터 일관성 체크 도구

## Phase 4: MongoDB 기반 AI/RAG 시스템 구현

### 4.1 MongoDB Vector Search 설정
- [ ] MongoDB Atlas Vector Search 설정
- [ ] 벡터 인덱스 생성 (768차원 임베딩)
- [ ] 임베딩 모델 선택 및 연동 (OpenAI, HuggingFace)
- [ ] MongoDB 벡터 저장 및 검색 유틸리티

### 4.2 하이브리드 검색 시스템
- [ ] MongoDB Text Search + Vector Search 결합
- [ ] Aggregation Pipeline 복합 검색 쿼리
- [ ] 검색 결과 스코어링 및 가중치 조정
- [ ] 실시간 검색 성능 최적화

### 4.3 RAG 시스템 구현
- [ ] 포트폴리오 문서 전처리 서비스
- [ ] MongoDB 문서 검색 (Retriever)
- [ ] LLM 연동 (OpenAI API, Anthropic Claude)
- [ ] 프롬프트 엔지니어링 및 응답 생성

### 4.4 포트폴리오 벡터화 자동화
- [ ] 포트폴리오 저장 시 자동 임베딩 생성
- [ ] MongoDB 벡터 필드 업데이트
- [ ] 백그라운드 벡터 인덱싱 작업
- [ ] 벡터 데이터 품질 관리

## Phase 5: 채용담당자 검색 기능 구현

### 5.1 MongoDB 기반 검색 API
- [ ] 자연어 검색 쿼리 처리 API
- [ ] MongoDB Aggregation 복합 검색
- [ ] 텍스트 + 벡터 하이브리드 검색
- [ ] 검색 결과 랭킹 및 정렬

### 5.2 고급 필터링 시스템
- [ ] MongoDB 필터링 (기술스택, 경력, 위치)
- [ ] 복합 조건 검색 (Aggregation Pipeline)
- [ ] 검색 성능 최적화 (인덱스 활용)
- [ ] 검색 결과 캐싱 (Redis)

### 5.3 구직자 정보 조회
- [ ] 제한적 정보 표시 API (개인정보 보호)
- [ ] MongoDB 문서 필드 마스킹
- [ ] 상세 프로필 조회 API
- [ ] 연락하기 기능 API

## Phase 6: 프론트엔드 연동 및 UI 구현 (병렬 작업, 각 Phase와 함께)

### 6.1 API 문서화 및 테스트
- [ ] Swagger UI를 통한 완전한 API 문서화
- [ ] API 테스트 케이스 작성
- [ ] Postman 컬렉션 생성

### 6.2 보안 및 성능 최적화
- [ ] API Rate Limiting 구현
- [ ] 데이터베이스 인덱싱 최적화
- [ ] Redis 캐싱 전략 구현
- [ ] 보안 헤더 설정

## Phase 7: 배포 및 운영 환경 구축 (2-3주)

### 7.1 컨테이너화 및 오케스트레이션
- [ ] Docker 이미지 최적화
- [ ] docker-compose를 통한 로컬 개발 환경
- [ ] AWS ECS 또는 EKS 설정

### 7.2 AWS 인프라 구축
- [ ] RDS (PostgreSQL) 설정
- [ ] ElastiCache (Redis) 설정
- [ ] S3 파일 저장소 설정
- [ ] CloudWatch 로깅 및 모니터링
- [ ] ALB/ELB 로드밸런서 설정

### 7.3 CI/CD 파이프라인
- [ ] GitHub Actions 또는 Jenkins 설정
- [ ] 자동화된 테스트 및 배포
- [ ] 환경별 배포 전략 (dev, staging, prod)

## Phase 8: 테스트 및 성능 최적화 (2주)

### 8.1 테스트 구현
- [ ] 단위 테스트 (JUnit 5)
- [ ] 통합 테스트
- [ ] API 테스트
- [ ] 성능 테스트

### 8.2 최종 최적화
- [ ] 쿼리 성능 최적화
- [ ] 캐싱 전략 최적화
- [ ] AI 모델 응답 시간 최적화
- [ ] 전체 시스템 성능 튜닝

## 예상 총 개발 기간: 약 3-4개월 (MongoDB 마이그레이션 포함)

### 우선순위별 개발 순서 (업데이트)
1. **High Priority**: ✅ Phase 1-2 완료 + 🔄 Phase 3 (MongoDB 통합)
2. **Medium Priority**: Phase 4-5 (MongoDB 기반 AI/RAG + 하이브리드 검색)
3. **Low Priority**: Phase 6-8 (UI + 배포 + 최적화)

### 현재 진행 상황
- ✅ **완료됨**: 기본 인프라, 사용자 관리, JWT 인증, PostgreSQL 설정
- 🔄 **진행 중**: MongoDB 통합, 포트폴리오 시스템, Document 모델링
- 📋 **예정**: AI/RAG 시스템, 하이브리드 검색 엔진

### 주요 기술적 도전 과제 (MongoDB 기반)
- **하이브리드 DB 아키텍처**: PostgreSQL + MongoDB 동시 관리
- **MongoDB Vector Search**: 768차원 벡터 인덱싱 및 검색 성능
- **Text + Vector 하이브리드**: 키워드 검색과 의미적 검색 결합
- **데이터 마이그레이션**: 기존 데이터의 안전한 MongoDB 이관
- **Aggregation Pipeline**: 복합 검색 쿼리 최적화
- **개인정보 보호**: MongoDB 문서 레벨 필드 마스킹
- **AI 모델 비용**: 벡터 임베딩 및 LLM API 비용 최적화

### MongoDB 도입으로 인한 추가 이점
- **유연한 스키마**: 다양한 포트폴리오 형태 지원
- **강력한 검색**: Text Search + Aggregation + Vector Search
- **확장성**: 대용량 포트폴리오 데이터 처리
- **AI 통합**: 벡터 데이터 네이티브 지원
- **성능**: 인덱스 최적화를 통한 빠른 검색