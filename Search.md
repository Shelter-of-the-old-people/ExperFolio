# 배경
 - ai서버를 구축하였다. 해당 서버에서 아래와 같이 `# 요청`에 맞게 보내면 `# 결과` 처럼 결과를 받아오는데 이걸 그대로 들어온 요청에 반환하면 되는 상황.

# 요청 

## 요청 API
 - http://localhost:8001/ai/search
 - POST

request body:
```json
{
  "query": "React와 TypeScript 경험이 있는 프론트엔드 개발자"
}
```

## 검색 프로세스:
    1. 검색 의도 분석 (GPT-4)
    2. 쿼리 임베딩 (KURE-v1)
    3. 벡터 유사도 검색 (MongoDB)
    4. 결과 재순위 (CrossEncoder)
    5. 매칭 분석 (GPT-4)
    
    Args:
        request: 검색 요청 (query 포함)
        search_service: 검색 서비스 (의존성 주입)
    
    Returns:
        SearchResponse: 검색 결과
    
    Raises:
        HTTPException: 검색 실패 시

# 결과


```json
{
    "status": "success",
    "candidates": [
        {
            "userId": "ehdgus-100",
            "matchScore": 0.9,
            "matchReason": "React와 TypeScript 경험은 포트폴리오의 'React와 TypeScript를 이용한 영화 검색 사이트 제작' 프로젝트에서 확인됩니다. 이 프로젝트에서는 타입 안정성을 높이고, 비동기적으로 데이터를 가져와 UI를 동적으로 변경하는 과정을 통해 프론트엔드 개발 역량을 보여주었습니다. 또한, '커머스 플랫폼 인턴십'에서는 상태 관리 로직 개선을 통해 효율적인 상태 관리 능력을 증명했습니다.",
            "keywords": [
                "React",
                "TypeScript",
                "영화 검색 사이트",
                "상태 관리",
                "Zustand"
            ]
        },
        {
            "userId": "alsgh-154",
            "matchScore": 0.9,
            "matchReason": "포트폴리오에서 'TypeScript와 React Query를 활용한 프론트엔드 개발' 경험은 쿼리의 핵심 요건인 React와 TypeScript 경험을 모두 충족합니다. React Query 사용을 통해 비동기 데이터 처리 능력을 보여주며, TypeScript 도입은 타입 안정성을 강조하여 코드 품질을 향상시킨 사례입니다. 이는 현대적인 프론트엔드 개발자로서의 역량을 충분히 증명합니다.",
            "keywords": [
                "TypeScript",
                "React",
                "React Query",
                "프론트엔드 개발",
                "비동기 데이터 통신"
            ]
        },
        {
            "userId": "ehdgus-60",
            "matchScore": 0.9,
            "matchReason": "React와 TypeScript 경험 모두 '뷰티 커머스 인턴십'에서의 'React 기반 디자인 시스템 컴포넌트 개발'로 확인됩니다. 특히 TypeScript와 Styled-Components를 사용해 컴포넌트를 개발한 경험은, 쿼리의 요구사항을 충족시키며, 컴포넌트 재사용성을 극대화한 Atomic Design 적용은 효율적인 프론트엔드 개발 역량을 증명합니다. JavaScript 기본기 또한 Vanilla JS 기반 SPA 개발로 뒷받침됩니다.",
            "keywords": [
                "React",
                "TypeScript",
                "뷰티 커머스 인턴십",
                "컴포넌트 개발",
                "Atomic Design"
            ]
        },
        {
            "userId": "ghfla-35",
            "matchScore": 0.9,
            "matchReason": "프론트엔드 개발자로서의 적합성은 포트폴리오의 'TypeScript 스터디' 항목에서 'Effective TypeScript' 완독 및 적용 경험으로 확인됩니다. 이는 TypeScript에 대한 깊은 이해와 실무 적용 경험을 보여주며, 특히 '투두리스트 앱의 모든 코드를 TypeScript로 작성'한 경험은 명확한 기술 역량을 증명합니다. React에 대한 구체적인 언급은 없지만, 전반적인 웹 개발 경험과 타입 안정성을 고려한 개발 경험이 이 요건을 어느 정도 충족한다고 볼 수 있습니다.",
            "keywords": [
                "TypeScript",
                "Effective TypeScript",
                "투두리스트 앱",
                "Zod",
                "타입 안정성"
            ]
        },
        {
            "userId": "ghfla-39",
            "matchScore": 0.7,
            "matchReason": "React 경험은 포트폴리오의 'React로 화면을 만들고'라는 표현을 통해 확인됩니다. 그러나 TypeScript 관련 경험은 명확히 언급되지 않았습니다. 프론트엔드 개발에 대한 열정과 다양한 테스트 프레임워크 활용 경험은 강점으로 보입니다.",
            "keywords": [
                "React",
                "프론트엔드 개발",
                "Jest",
                "Cypress",
                "E2E 테스트"
            ]
        },
        {
            "userId": "ghfla-55",
            "matchScore": 0.1,
            "matchReason": "포트폴리오에는 React Native 관련 경험이 언급되어 있지만, 쿼리의 핵심 요구 사항인 React와 TypeScript에 대한 경험은 명시적으로 확인되지 않았습니다. 따라서 필수 요구 사항인 React와 TypeScript 경험이 부족합니다.",
            "keywords": [
                "React Native",
                "비동기 처리",
                "성능 최적화"
            ]
        },
        {
            "userId": "ghfla-281",
            "matchScore": 0.9,
            "matchReason": "React와 TypeScript 경험은 포트폴리오에서 'React Native 프로젝트에 TypeScript 적용' 문구로 확인됩니다. 특히, TypeScript 기반으로 전환하는 작업을 주도하며 타입 안전성을 강화한 경험은 중요한 역량을 보여줍니다. React 관련 경험은 직접 언급되지 않았지만, TypeScript와 함께 한 React Native 개발 경험은 React 활용 능력을 짐작할 수 있게 합니다.",
            "keywords": [
                "TypeScript",
                "React Native",
                "타입 안전성",
                "컴파일 시점 오류 발견",
                "캡스톤 프로젝트"
            ]
        },
        {
            "userId": "ghfla-56",
            "matchScore": 0.4,
            "matchReason": "포트폴리오에서는 React에 대한 경험이 'Context API를 이용한 간단한 상태 관리' 프로젝트를 통해 일부 확인되지만, TypeScript에 대한 명시적 언급은 없습니다. React와 관련된 프로젝트 경험은 있으나, 쿼리에서 요구하는 TypeScript 경험이 부족합니다.",
            "keywords": [
                "React",
                "Context API",
                "상태 관리"
            ]
        },
        {
            "userId": "ghfla-41",
            "matchScore": 0.5,
            "matchReason": "포트폴리오에서 'React 입문: 영화 정보 사이트 클론 코딩' 프로젝트는 React 경험을 확인할 수 있는 중요한 증거입니다. React의 기본 Hook 사용법과 컴포넌트 기반 개발 방식에 대한 이해를 보여줍니다. 그러나 TypeScript에 대한 언급이나 경험은 포트폴리오에서 찾아볼 수 없습니다.",
            "keywords": [
                "React",
                "영화 정보 사이트",
                "클론 코딩",
                "컴포넌트 기반 개발",
                "Hook 사용"
            ]
        },
        {
            "userId": "alsgh-134",
            "matchScore": 0.9,
            "matchReason": "React와 TypeScript 관련 경험은 'Next.js와 TypeScript를 활용한 개인 기술 블로그 제작' 프로젝트에서 확인됩니다. 이 프로젝트는 TypeScript를 사용하여 컴파일 단계에서 오류를 방지하고, 웹 성능 최적화에 집중한 사례로, 프론트엔드 개발 역량을 명확히 보여줍니다. React 관련 경험은 명시되지 않았지만, Next.js 프레임워크 사용에서 React의 사용이 암시됩니다.",
            "keywords": [
                "Next.js",
                "TypeScript",
                "프론트엔드 개발",
                "웹 성능 최적화",
                "SSR"
            ]
        }
    ],
    "searchTime": "8.17s",
    "totalResults": 10
}
```

---

# 구현 계획 (Implementation Plan)

## 개요
AI 서버(`http://localhost:8001/ai/search`)와 통합하여 검색 기능을 제공하는 API를 구현합니다.
Spring Boot에서 요청을 받아 AI 서버로 전달하고, 결과를 그대로 반환하는 프록시 역할을 수행합니다.

## 아키텍처
```
Client → SearchController → SearchService → AI Server (localhost:8001)
                ↓                ↓
            SearchRequestDto   RestTemplate
            SearchResponseDto
```

## 구현 단계

### 1. DTO 생성 (`domain/search/dto/`)
- [x] `SearchRequestDto.java` - 검색 요청 DTO
  - `String query` - 검색 쿼리

- [x] `SearchResponseDto.java` - 검색 응답 DTO
  - `String status` - 응답 상태
  - `List<CandidateDto> candidates` - 후보자 목록
  - `String searchTime` - 검색 소요 시간
  - `Integer totalResults` - 전체 결과 수

- [x] `CandidateDto.java` - 후보자 정보 DTO
  - `String userId` - 사용자 ID
  - `Double matchScore` - 매칭 점수 (0.0 ~ 1.0)
  - `String matchReason` - 매칭 이유
  - `List<String> keywords` - 키워드 목록

### 2. Service 레이어 (`domain/search/service/`)
- [x] `SearchService.java` - 검색 서비스 인터페이스
  - `SearchResponseDto search(String query)` - 검색 실행

- [x] `SearchServiceImpl.java` - 검색 서비스 구현
  - RestTemplate을 사용하여 AI 서버로 HTTP POST 요청
  - 에러 핸들링 (타임아웃, 연결 실패 등)
  - 로깅

### 3. Controller 레이어 (`domain/search/controller/`)
- [x] `SearchController.java` - 검색 API 컨트롤러
  - `POST /api/v1/search` - 검색 엔드포인트
  - JWT 인증 필요 (RECRUITER 역할만 허용)
  - Request Body: `SearchRequestDto`
  - Response Body: `SearchResponseDto`
  - Swagger 문서화

### 4. Configuration (`global/config/`)
- [x] `RestTemplateConfig.java` - RestTemplate 빈 설정
  - Connection timeout: 5초
  - Read timeout: 30초 (AI 처리 시간 고려)

### 5. Exception Handling
- [x] `SearchServiceException.java` - 검색 서비스 예외
- [x] GlobalExceptionHandler에 검색 관련 예외 추가

## API 명세

### Endpoint
```
POST /api/v1/search
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

### Request
```json
{
  "query": "React와 TypeScript 경험이 있는 프론트엔드 개발자"
}
```

### Response (200 OK)
```json
{
  "status": "success",
  "candidates": [
    {
      "userId": "ehdgus-100",
      "matchScore": 0.9,
      "matchReason": "React와 TypeScript 경험...",
      "keywords": ["React", "TypeScript", "영화 검색 사이트"]
    }
  ],
  "searchTime": "8.17s",
  "totalResults": 10
}
```

### Error Responses
- **400 Bad Request**: 쿼리가 비어있거나 유효하지 않음
- **401 Unauthorized**: JWT 토큰이 없거나 유효하지 않음
- **403 Forbidden**: RECRUITER 역할이 아님
- **500 Internal Server Error**: AI 서버 연결 실패 또는 내부 오류
- **503 Service Unavailable**: AI 서버가 응답하지 않음

## 환경 설정

### application.yml 추가
```yaml
ai:
  server:
    url: http://localhost:8001
    search-endpoint: /ai/search
    timeout:
      connect: 5000  # 5 seconds
      read: 30000    # 30 seconds
```

### .env 추가 (선택)
```env
AI_SERVER_URL=http://localhost:8001
AI_SERVER_TIMEOUT_CONNECT=5000
AI_SERVER_TIMEOUT_READ=30000
```

## 보안 고려사항
1. **접근 제어**: RECRUITER 역할만 검색 가능
2. **Rate Limiting**: 향후 추가 예정 (분당 요청 제한)
3. **Query Validation**: 쿼리 길이 제한 (최대 500자)
4. **Logging**: 검색 쿼리와 결과를 로깅 (개인정보 마스킹)

## 테스트 계획
1. **단위 테스트**:
   - SearchServiceImpl 테스트 (MockRestTemplate)
   - DTO validation 테스트

2. **통합 테스트**:
   - SearchController 테스트 (MockMvc)
   - AI 서버 Mock 테스트

3. **수동 테스트**:
   - Swagger UI에서 실제 요청 테스트
   - Postman으로 엔드포인트 테스트

## 향후 개선 사항
- [ ] 검색 결과 캐싱 (Redis)
- [ ] 검색 히스토리 저장
- [ ] 페이지네이션 지원
- [ ] 필터링 옵션 추가 (matchScore 최소값 등)
- [ ] 비동기 검색 지원 (WebFlux)

---

**작성일**: 2025-01-01
**상태**: 구현 준비 완료