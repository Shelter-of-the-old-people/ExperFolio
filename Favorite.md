# Favorite (즐겨찾기) 기능 설계 문서

## 개요

리크루터가 마음에 드는 구직자의 포트폴리오를 즐겨찾기하여 관리할 수 있는 기능입니다.

**작성일**: 2025-11-30
**상태**: 설계 단계 (미구현)
**우선순위**: Medium

---

## 기능 요구사항 (MVP - 최소 기능)

### 핵심 기능 (Phase 1 - 필수)
1. ✅ **즐겨찾기 추가**: 리크루터가 구직자를 즐겨찾기에 추가
2. ✅ **즐겨찾기 제거**: 즐겨찾기에서 구직자 제거
3. ✅ **즐겨찾기 목록 조회**: 내가 즐겨찾기한 구직자 목록 확인 (페이징 지원)
4. ✅ **즐겨찾기 여부 확인**: 특정 구직자가 즐겨찾기되어 있는지 확인

### 제외된 기능 (Phase 2 - 향후 확장)
- ❌ 메모 기능
- ❌ 폴더/카테고리 관리
- ❌ 태그 기능
- ❌ 통계 API
- ❌ 알림 기능
- ❌ 공유 기능

**설계 원칙**: 가장 단순한 형태로 시작하여 동작을 검증한 후, 필요에 따라 기능 추가

---

## 현재 프로젝트 상태 분석

### ✅ 구현된 기능
- **User Management**: User, JobSeekerProfile, RecruiterProfile 엔티티 존재
- **Portfolio**: MongoDB에 포트폴리오 저장
- **Search**: 리크루터가 구직자 검색 기능 (`/api/v1/search`)
- **Portfolio 조회**: 리크루터가 특정 구직자 포트폴리오 조회 (`GET /api/portfolios/{userId}`)

### ❌ 구현되지 않은 기능
- **Favorite 도메인**: 전혀 구현되지 않음
- **즐겨찾기 테이블**: PostgreSQL에 favorite 테이블 없음
- **즐겨찾기 API**: 관련 엔드포인트 없음

---

## 데이터베이스 설계 (MVP - 간소화)

### PostgreSQL 테이블: `favorites`

```sql
CREATE TABLE favorites (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recruiter_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_seeker_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 제약조건
    CONSTRAINT unique_favorite UNIQUE (recruiter_id, job_seeker_id),
    CONSTRAINT check_different_users CHECK (recruiter_id != job_seeker_id)
);

-- 인덱스
CREATE INDEX idx_favorites_recruiter_id ON favorites(recruiter_id);
CREATE INDEX idx_favorites_created_at ON favorites(created_at DESC);
```

### 테이블 설계 포인트

1. **최소 필드만 포함**: id, recruiter_id, job_seeker_id, created_at만 사용
2. **복합 유니크 키**: 한 리크루터가 같은 구직자를 중복 즐겨찾기 불가
3. **CASCADE DELETE**: 사용자 삭제 시 관련 즐겨찾기 자동 삭제
4. **CHECK 제약조건**: 자기 자신을 즐겨찾기할 수 없음
5. **인덱스 최소화**:
   - `recruiter_id`: 내 즐겨찾기 목록 조회 시 필수
   - `created_at`: 최근 추가된 순서로 정렬 (기본 정렬)

### Phase 2에서 추가 가능한 필드
```sql
-- 추후 ALTER TABLE로 추가 가능
ALTER TABLE favorites ADD COLUMN memo TEXT;
ALTER TABLE favorites ADD COLUMN folder_name VARCHAR(100);
ALTER TABLE favorites ADD COLUMN tags TEXT[];
ALTER TABLE favorites ADD COLUMN updated_at TIMESTAMP;
```

---

## 도메인 구조

### 디렉토리 구조
```
src/main/java/com/example/experfolio/domain/favorite/
├── entity/
│   └── Favorite.java                  # PostgreSQL 엔티티
├── repository/
│   └── FavoriteRepository.java        # Spring Data JPA Repository
├── service/
│   ├── FavoriteService.java           # 인터페이스
│   └── FavoriteServiceImpl.java       # 구현체
├── controller/
│   └── FavoriteController.java        # REST API 컨트롤러
└── dto/
    ├── FavoriteRequestDto.java        # 즐겨찾기 추가 요청
    ├── FavoriteResponseDto.java       # 즐겨찾기 응답
    ├── FavoriteListResponseDto.java   # 즐겨찾기 목록 응답
    └── FavoriteStatsDto.java          # 즐겨찾기 통계
```

---

## 엔티티 설계 (MVP - 간소화)

### Favorite.java

```java
@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Favorite(User recruiter, User jobSeeker) {
        validateUsers(recruiter, jobSeeker);
        this.recruiter = recruiter;
        this.jobSeeker = jobSeeker;
    }

    // 비즈니스 로직
    private void validateUsers(User recruiter, User jobSeeker) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new IllegalArgumentException("리크루터만 즐겨찾기를 추가할 수 있습니다");
        }
        if (jobSeeker.getRole() != UserRole.JOB_SEEKER) {
            throw new IllegalArgumentException("구직자만 즐겨찾기할 수 있습니다");
        }
        if (recruiter.getId().equals(jobSeeker.getId())) {
            throw new IllegalArgumentException("자기 자신을 즐겨찾기할 수 없습니다");
        }
    }
}
```

**제거된 필드**:
- ❌ `memo` (메모 기능 제거)
- ❌ `folderName` (폴더 기능 제거)
- ❌ `tags` (태그 기능 제거)
- ❌ `updatedAt` (수정 기능이 없으므로 불필요)

---

## API 설계 (MVP - 4개 엔드포인트만)

### 베이스 경로
```
/api/v1/favorites
```

### 엔드포인트 목록

#### 1. 즐겨찾기 추가
```
POST /api/v1/favorites
```

**요청**:
```json
{
  "jobSeekerId": "uuid-string"
}
```

**응답** (201 Created):
```json
{
  "id": "favorite-uuid",
  "jobSeeker": {
    "id": "jobseeker-uuid",
    "name": "김개발",
    "desiredPosition": "백엔드 개발자",
    "major": "컴퓨터공학",
    "schoolName": "한국대학교",
    "gpa": 3.8
  },
  "createdAt": "2025-11-30T10:30:00"
}
```

**에러**:
- `400 Bad Request`: 이미 즐겨찾기된 구직자
- `403 Forbidden`: 리크루터가 아닌 사용자
- `404 Not Found`: 존재하지 않는 구직자 또는 구직자가 아닌 사용자

---

#### 2. 즐겨찾기 제거
```
DELETE /api/v1/favorites/{jobSeekerId}
```

**Path Parameter**:
- `jobSeekerId`: 즐겨찾기 해제할 구직자 ID

**응답** (204 No Content):
```
(empty body)
```

**에러**:
- `404 Not Found`: 즐겨찾기가 존재하지 않음

---

#### 3. 즐겨찾기 목록 조회 (페이징)
```
GET /api/v1/favorites?page=0&size=20&sort=createdAt,desc
```

**Query Parameters**:
- `page`: 페이지 번호 (0부터 시작, 기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 기준 (기본값: createdAt,desc)

**응답** (200 OK):
```json
{
  "content": [
    {
      "id": "favorite-uuid",
      "jobSeeker": {
        "id": "jobseeker-uuid",
        "name": "김개발",
        "desiredPosition": "백엔드 개발자",
        "major": "컴퓨터공학",
        "schoolName": "한국대학교",
        "gpa": 3.8
      },
      "createdAt": "2025-11-30T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0
}
```

---

#### 4. 즐겨찾기 여부 확인
```
GET /api/v1/favorites/{jobSeekerId}/exists
```

**Path Parameter**:
- `jobSeekerId`: 확인할 구직자 ID

**응답** (200 OK):
```json
{
  "exists": true,
  "favoriteId": "favorite-uuid",
  "createdAt": "2025-11-30T10:30:00"
}
```

또는

```json
{
  "exists": false
}
```

---

### ❌ 제외된 API (Phase 2)
- `PATCH /api/v1/favorites/{jobSeekerId}/memo` - 메모 수정
- `GET /api/v1/favorites/stats` - 통계 조회

---

## DTO 설계 (MVP - 간소화)

### FavoriteRequestDto.java
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequestDto {

    @NotBlank(message = "구직자 ID는 필수입니다")
    private String jobSeekerId;
}
```

### FavoriteResponseDto.java
```java
@Getter
@Builder
public class FavoriteResponseDto {
    private String id;
    private JobSeekerInfoDto jobSeeker;
    private LocalDateTime createdAt;
}
```

### JobSeekerInfoDto.java
```java
@Getter
@Builder
public class JobSeekerInfoDto {
    private String id;
    private String name;
    private String desiredPosition;
    private String major;
    private String schoolName;
    private Double gpa;
}
```

### FavoriteExistsDto.java
```java
@Getter
@Builder
public class FavoriteExistsDto {
    private Boolean exists;
    private String favoriteId;  // exists가 true일 때만 포함
    private LocalDateTime createdAt;  // exists가 true일 때만 포함
}
```

**제거된 필드**:
- ❌ `memo` (메모 기능 제거)
- ❌ `recruiter` (응답에서 recruiter 정보 제거 - 본인이므로 불필요)
- ❌ `updatedAt` (수정 기능 없으므로 불필요)

---

## 비즈니스 로직 (MVP - 간소화)

### FavoriteService 인터페이스

```java
public interface FavoriteService {

    /**
     * 즐겨찾기 추가
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @return 생성된 즐겨찾기 정보
     * @throws DuplicateFavoriteException 중복 즐겨찾기
     * @throws IllegalArgumentException 유효하지 않은 사용자
     */
    FavoriteResponseDto addFavorite(String recruiterId, String jobSeekerId);

    /**
     * 즐겨찾기 제거
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @throws FavoriteNotFoundException 즐겨찾기가 존재하지 않음
     */
    void removeFavorite(String recruiterId, String jobSeekerId);

    /**
     * 즐겨찾기 목록 조회 (페이징)
     * @param recruiterId 리크루터 ID
     * @param pageable 페이징 정보
     * @return 즐겨찾기 목록
     */
    Page<FavoriteResponseDto> getFavorites(String recruiterId, Pageable pageable);

    /**
     * 즐겨찾기 여부 확인
     * @param recruiterId 리크루터 ID
     * @param jobSeekerId 구직자 ID
     * @return 즐겨찾기 존재 여부 및 정보
     */
    FavoriteExistsDto checkFavoriteExists(String recruiterId, String jobSeekerId);
}
```

**제거된 메서드**:
- ❌ `updateMemo()` (메모 수정 기능 제거)
- ❌ `getStats()` (통계 기능 제거)

---

## 예외 처리

### 커스텀 예외

```java
public class DuplicateFavoriteException extends RuntimeException {
    public DuplicateFavoriteException(String message) {
        super(message);
    }
}

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler 추가

```java
@ExceptionHandler(DuplicateFavoriteException.class)
public ResponseEntity<ErrorResponse> handleDuplicateFavorite(DuplicateFavoriteException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("DUPLICATE_FAVORITE", e.getMessage()));
}

@ExceptionHandler(FavoriteNotFoundException.class)
public ResponseEntity<ErrorResponse> handleFavoriteNotFound(FavoriteNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("FAVORITE_NOT_FOUND", e.getMessage()));
}
```

---

## 보안 및 권한

### 접근 제어
- **모든 즐겨찾기 API**: `@PreAuthorize("hasRole('RECRUITER')")` 필수
- **본인 확인**: Service 레이어에서 recruiterId와 JWT의 userId 일치 여부 확인
- **구직자 검증**: 즐겨찾기 대상이 실제 JOB_SEEKER 역할인지 확인

### 데이터 보호
- **Soft Delete 사용자**: deletedAt이 NULL이 아닌 사용자는 즐겨찾기 불가
- **비활성 사용자**: 탈퇴한 사용자의 즐겨찾기는 CASCADE DELETE로 자동 삭제

---

## 성능 최적화

### 1. 인덱스 활용
- `recruiter_id` 인덱스로 목록 조회 최적화
- 복합 유니크 인덱스로 중복 체크 최적화

### 2. N+1 문제 방지
```java
@Query("SELECT f FROM Favorite f " +
       "JOIN FETCH f.recruiter " +
       "JOIN FETCH f.jobSeeker " +
       "WHERE f.recruiter.id = :recruiterId")
Page<Favorite> findByRecruiterId(@Param("recruiterId") UUID recruiterId, Pageable pageable);
```

### 3. 캐싱 전략 (향후 적용)
- Redis를 활용한 즐겨찾기 여부 캐싱
- 통계 정보 캐싱 (5분 TTL)

---

## 테스트 케이스

### 단위 테스트 (Service Layer)

```java
@Test
void addFavorite_성공() {
    // Given
    String recruiterId = "recruiter-uuid";
    String jobSeekerId = "jobseeker-uuid";
    String memo = "좋은 후보";

    // When
    FavoriteResponseDto result = favoriteService.addFavorite(recruiterId, jobSeekerId, memo);

    // Then
    assertThat(result.getMemo()).isEqualTo(memo);
    assertThat(result.getJobSeeker().getId()).isEqualTo(jobSeekerId);
}

@Test
void addFavorite_중복_실패() {
    // Given
    String recruiterId = "recruiter-uuid";
    String jobSeekerId = "jobseeker-uuid";
    favoriteService.addFavorite(recruiterId, jobSeekerId, "첫 번째");

    // When & Then
    assertThatThrownBy(() ->
        favoriteService.addFavorite(recruiterId, jobSeekerId, "두 번째")
    ).isInstanceOf(DuplicateFavoriteException.class);
}

@Test
void addFavorite_구직자가_아닌_사용자_실패() {
    // Given
    String recruiterId = "recruiter-uuid";
    String anotherRecruiterId = "another-recruiter-uuid";

    // When & Then
    assertThatThrownBy(() ->
        favoriteService.addFavorite(recruiterId, anotherRecruiterId, "메모")
    ).isInstanceOf(IllegalArgumentException.class)
     .hasMessageContaining("구직자만");
}
```

### 통합 테스트 (Controller Layer)

```java
@Test
@WithMockUser(username = "recruiter-uuid", roles = {"RECRUITER"})
void POST_즐겨찾기_추가_성공() throws Exception {
    // Given
    FavoriteRequestDto request = new FavoriteRequestDto("jobseeker-uuid", "메모");

    // When & Then
    mockMvc.perform(post("/api/v1/favorites")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.jobSeeker.id").value("jobseeker-uuid"))
        .andExpect(jsonPath("$.memo").value("메모"));
}

@Test
@WithMockUser(username = "jobseeker-uuid", roles = {"JOB_SEEKER"})
void POST_즐겨찾기_추가_권한없음_실패() throws Exception {
    // Given
    FavoriteRequestDto request = new FavoriteRequestDto("jobseeker-uuid", "메모");

    // When & Then
    mockMvc.perform(post("/api/v1/favorites")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
}
```

---

## 마이그레이션 파일 (MVP - 간소화)

### V3__create_favorites_table.sql

```sql
-- Favorites 테이블 생성 (MVP - 최소 기능)
CREATE TABLE favorites (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recruiter_id        UUID NOT NULL,
    job_seeker_id       UUID NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래키
    CONSTRAINT fk_favorites_recruiter FOREIGN KEY (recruiter_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_job_seeker FOREIGN KEY (job_seeker_id)
        REFERENCES users(id) ON DELETE CASCADE,

    -- 제약조건
    CONSTRAINT unique_favorite UNIQUE (recruiter_id, job_seeker_id),
    CONSTRAINT check_different_users CHECK (recruiter_id != job_seeker_id)
);

-- 인덱스 생성 (최소한만)
CREATE INDEX idx_favorites_recruiter_id ON favorites(recruiter_id);
CREATE INDEX idx_favorites_created_at ON favorites(created_at DESC);

-- 코멘트 추가
COMMENT ON TABLE favorites IS '리크루터의 구직자 즐겨찾기 (MVP)';
COMMENT ON COLUMN favorites.recruiter_id IS '즐겨찾기를 추가한 리크루터';
COMMENT ON COLUMN favorites.job_seeker_id IS '즐겨찾기된 구직자';
COMMENT ON COLUMN favorites.created_at IS '즐겨찾기 추가 시점';
```

### Phase 2 확장 예시
```sql
-- 추후 필요 시 아래 명령으로 컬럼 추가 가능
ALTER TABLE favorites ADD COLUMN memo TEXT;
ALTER TABLE favorites ADD COLUMN folder_name VARCHAR(100);
ALTER TABLE favorites ADD COLUMN tags TEXT[];
ALTER TABLE favorites ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 추가 인덱스
CREATE INDEX idx_favorites_job_seeker_id ON favorites(job_seeker_id);
```

---

## 구현 우선순위

### Phase 1: 기본 기능 (필수)
1. ✅ **데이터베이스 설계**: 마이그레이션 파일 작성
2. ✅ **엔티티 생성**: Favorite.java
3. ✅ **Repository**: FavoriteRepository 인터페이스
4. ✅ **Service**: FavoriteService, FavoriteServiceImpl
5. ✅ **Controller**: FavoriteController
6. ✅ **DTO**: Request/Response DTO 작성
7. ✅ **예외 처리**: 커스텀 예외 및 GlobalExceptionHandler 업데이트
8. ✅ **테스트**: 단위 테스트 및 통합 테스트

### Phase 2: 확장 기능 (선택)
- 폴더 관리
- 태그 기능
- 메모 검색
- 알림 기능
- 공유 기능

---

## 참고 사항

### 기존 기능과의 통합

1. **Search API와 연계**:
   - 검색 결과에서 즐겨찾기 여부 표시
   - 즐겨찾기한 구직자만 필터링

2. **Portfolio API와 연계**:
   - 포트폴리오 조회 시 즐겨찾기 여부 포함
   - 즐겨찾기 목록에서 포트폴리오 미리보기

3. **통계 대시보드**:
   - 리크루터 대시보드에 즐겨찾기 통계 표시
   - 인기 구직자 순위 (많이 즐겨찾기된 구직자)

---

## 의사결정 필요 사항

- [ ] **폴더 기능**: Phase 1에 포함할지, Phase 2로 연기할지
- [ ] **메모 길이 제한**: 1000자가 적당한지 (현재 TEXT 타입 - 무제한)
- [ ] **페이징 기본값**: 페이지당 20개가 적당한지
- [ ] **정렬 옵션**: 추가 정렬 기준이 필요한지 (GPA 순, 학교 순 등)
- [ ] **즐겨찾기 제한**: 리크루터당 최대 즐겨찾기 개수 제한이 필요한지
- [ ] **알림 기능**: 구직자가 포트폴리오 업데이트 시 리크루터에게 알림 여부

---

## 다음 단계

1. **설계 검토**: 이 문서를 기반으로 설계 검토 및 피드백
2. **의사결정**: 위의 의사결정 사항 확정
3. **마이그레이션 파일 작성**: PostgreSQL 스키마 생성
4. **엔티티 및 Repository 구현**: 기본 CRUD 기능
5. **Service 구현**: 비즈니스 로직 작성
6. **Controller 구현**: REST API 엔드포인트
7. **테스트 작성**: 단위/통합 테스트
8. **Swagger 문서화**: API 문서 자동 생성 확인
9. **통합 테스트**: Postman/Swagger UI로 전체 플로우 테스트

---

**작성자**: Claude Code
**마지막 업데이트**: 2025-11-30
