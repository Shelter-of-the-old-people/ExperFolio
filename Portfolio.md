# Portfolio API Documentation

## 기존 API

### GET /api/portfolios/me
- **설명**: 현재 로그인한 사용자의 포트폴리오 조회
- **인증**: JWT 토큰 필요
- **권한**: JOB_SEEKER만 가능
- **응답**: 전체 포트폴리오 정보

---

## 신규 API 설계

### GET /api/portfolios/{user-id}

#### 개요
특정 사용자의 포트폴리오를 조회하는 API

#### 목적
- 리크루터가 구직자의 포트폴리오를 열람
- 포트폴리오 공유 링크 기능 (향후 확장 가능)

#### 엔드포인트
```
GET /api/portfolios/{user-id}
```

#### Path Parameters
- `user-id` (String, required): 조회할 사용자의 ID (UUID 형식 권장, 하지만 형식 검증 없이 조회 진행)

#### 인증 및 권한
**옵션 1: 리크루터 전용 (권장)**
- **인증**: JWT 토큰 필수
- **권한**: RECRUITER 역할만 접근 가능
- **이유**: 개인정보 보호 및 무분별한 접근 방지
- **장점**: 보안성 강화, 추후 조회 이력 추적 가능

**옵션 2: 인증된 사용자**
- **인증**: JWT 토큰 필수
- **권한**: 모든 역할 접근 가능 (본인 포트폴리오 조회 시 전체 정보, 타인 조회 시 제한된 정보)
- **이유**: 유연한 접근 제어
- **장점**: 향후 구직자 간 포트폴리오 공유 기능 확장 가능

**옵션 3: 공개 API (비권장)**
- **인증**: 불필요
- **권한**: 누구나 접근 가능
- **위험**: 개인정보 노출, 크롤링 위험
- **사용 케이스**: 포트폴리오 공개 링크 기능 (별도의 공개 설정 필드 필요)

**추천**: 옵션 1 (리크루터 전용) 선택 후, 향후 필요 시 공개/비공개 설정 추가

#### 응답 데이터
**전체 정보 포함 (본인 또는 권한 있는 리크루터)**
```json
{
  "userId": "uuid",
  "basicInfo": {
    "name": "홍길동",
    "schoolName": "한국대학교",
    "major": "컴퓨터공학",
    "gpa": 3.8,
    "desiredPosition": "백엔드 개발자",
    "referenceUrl": ["https://github.com/user"],
    "awards": [...],
    "certifications": [...],
    "languages": [...]
  },
  "portfolioItems": [
    {
      "id": "uuid",
      "order": 1,
      "type": "project",
      "title": "프로젝트 제목",
      "content": "프로젝트 내용...",
      "attachments": [...],
      "createdAt": "2024-10-28T...",
      "updatedAt": "2024-10-28T..."
    }
  ],
  "createdAt": "2024-10-28T...",
  "updatedAt": "2024-10-28T..."
}
```

**제한된 정보 (향후 개인정보 보호 적용 시)**
- 민감한 개인정보 제외 (전화번호, 이메일 등은 PostgreSQL의 JobSeekerProfile에 있으므로 현재는 해당 없음)
- 파일 다운로드 URL 제한 또는 워터마크 추가 (향후 구현)

#### 에러 처리
- **404 Not Found**: 해당 user-id를 가진 사용자가 존재하지 않음 (잘못된 형식의 ID 포함)
- **404 Not Found**: 해당 사용자가 포트폴리오를 생성하지 않음
- **403 Forbidden**: 권한이 없는 사용자 (리크루터 전용 API인 경우)
- **401 Unauthorized**: 인증되지 않은 요청 (JWT 토큰 없음)

**참고**: userId의 형식 검증을 하지 않고, 단순히 조회를 시도합니다. 잘못된 형식의 ID는 자연스럽게 "존재하지 않음"으로 처리되어 404를 반환합니다.

#### 구현 계획

**1. Controller Layer** (`PortfolioController.java`)
```java
@GetMapping("/{userId}")
@PreAuthorize("hasRole('RECRUITER')") // 옵션 1 선택 시
public ResponseEntity<PortfolioResponseDto> getPortfolioByUserId(
    @PathVariable("userId") String userId,
    @AuthenticationPrincipal UserDetails currentUser
) {
    // Service 호출
}
```

**2. Service Layer** (`PortfolioService.java`, `PortfolioServiceImpl.java`)
- `Portfolio findByUserId(String userId)` 메서드 추가
- 사용자 존재 여부 확인 (UserRepository 사용)
- 포트폴리오 존재 여부 확인 (PortfolioRepository 사용)
- 권한 검증 로직 (필요 시)

**3. Repository Layer** (`PortfolioRepository.java`)
- 기존 `Optional<Portfolio> findByUserId(String userId)` 메서드 활용
- 추가 메서드 불필요 (이미 존재할 가능성 높음)

**4. DTO**
- 기존 `PortfolioResponseDto` 재사용
- 향후 필요 시 `PublicPortfolioResponseDto` 생성 (민감 정보 제외)

#### 보안 고려사항
1. **접근 제어**
   - Spring Security `@PreAuthorize` 어노테이션 사용
   - RECRUITER 역할만 접근 가능하도록 제한

2. **사용자 검증**
   - Path parameter의 userId가 실제 존재하는 사용자인지 확인
   - 해당 사용자가 JOB_SEEKER 역할인지 확인 (리크루터 포트폴리오는 조회 불가)

3. **데이터 필터링**
   - 현재는 Portfolio 문서에 민감 정보가 없으므로 전체 반환
   - 향후 필요 시 개인정보 필터링 로직 추가

4. **Rate Limiting** (향후 구현)
   - 무분별한 포트폴리오 크롤링 방지
   - IP 기반 또는 사용자 기반 요청 제한

5. **조회 로그** (향후 구현)
   - 누가, 언제, 누구의 포트폴리오를 조회했는지 기록
   - 구직자가 자신의 포트폴리오 조회 이력 확인 가능

#### 테스트 케이스
1. **정상 케이스**
   - 리크루터가 존재하는 구직자의 포트폴리오 조회 → 200 OK

2. **에러 케이스**
   - 존재하지 않는 userId → 404 Not Found
   - 잘못된 형식의 userId (UUID가 아닌 경우) → 404 Not Found (존재하지 않는 것으로 처리)
   - 포트폴리오가 없는 사용자 → 404 Not Found
   - 리크루터가 아닌 사용자 접근 → 403 Forbidden
   - 인증되지 않은 요청 → 401 Unauthorized
   - 리크루터의 포트폴리오 조회 시도 → 403 Forbidden or 404

3. **경계 케이스**
   - 본인의 포트폴리오를 /api/portfolios/{user-id}로 조회 (허용)
   - soft delete된 사용자의 포트폴리오 조회 → 404 Not Found

#### 향후 확장 가능성
1. **공개/비공개 설정**
   - Portfolio 문서에 `isPublic` 필드 추가
   - 공개 설정 시 인증 없이 조회 가능한 별도 엔드포인트 생성 (예: `/api/portfolios/public/{user-id}`)

2. **조회 통계**
   - 포트폴리오 조회 수, 조회자 정보 저장
   - 구직자 대시보드에서 조회 통계 확인

3. **개인정보 보호 강화**
   - 리크루터의 회사 인증 여부에 따라 접근 권한 차등 적용
   - 특정 항목만 선택적으로 공개하는 기능

4. **PDF 다운로드**
   - 포트폴리오 전체를 PDF로 변환하여 다운로드
   - 워터마크 또는 조회자 정보 삽입

#### 의사결정 필요 사항
- [ ] 접근 권한: 리크루터 전용 vs 모든 인증된 사용자
- [ ] 응답 데이터: 전체 정보 vs 필터링된 정보
- [ ] 사용자 역할 검증: 구직자만 포트폴리오 조회 가능하도록 할지
- [ ] 에러 메시지: 보안을 위해 "존재하지 않음"과 "권한 없음"을 구분할지 통일할지

---

## 첨부파일 개별 삭제 API

### DELETE /api/portfolios/items/{itemId}/attachments

#### 개요
포트폴리오 아이템에 첨부된 특정 파일만 개별적으로 삭제하는 API

#### 배경
- 현재는 아이템 전체 삭제 시에만 첨부파일 삭제 가능
- 사용자가 아이템은 유지하고 특정 파일만 제거하고 싶을 수 있음
- 잘못 업로드한 파일을 삭제할 수 있어야 함

#### 엔드포인트
```
DELETE /api/portfolios/items/{itemId}/attachments?objectKey={objectKey}
```

#### Path Parameters
- `itemId` (String, required): 포트폴리오 아이템 ID

#### Query Parameters
- `objectKey` (String, required): 삭제할 첨부파일의 R2 object key

#### 인증 및 권한
- **인증**: JWT 토큰 필수
- **권한**: JOB_SEEKER (본인의 포트폴리오만)
- **검증**: 해당 아이템이 본인의 포트폴리오에 속해있는지 확인

#### 요청 예시
```http
DELETE /api/portfolios/items/abc-123-def/attachments?objectKey=users/uuid/filename.pdf
Authorization: Bearer {access_token}
```

#### 응답

**성공 (204 No Content)**
```
(empty body)
```

**실패 예시**

1. 포트폴리오를 찾을 수 없음 (400 Bad Request)
```json
{
  "error": "포트폴리오를 찾을 수 없습니다"
}
```

2. 아이템을 찾을 수 없음 (404 Not Found)
```json
{
  "error": "포트폴리오 아이템을 찾을 수 없습니다"
}
```

3. 첨부파일을 찾을 수 없음 (404 Not Found)
```json
{
  "error": "첨부파일을 찾을 수 없습니다"
}
```

4. 권한 없음 (403 Forbidden)
```json
{
  "error": "해당 포트폴리오에 대한 권한이 없습니다"
}
```

#### 구현 계획

**1. Controller Layer** (`PortfolioController.java`)
```java
/**
 * 포트폴리오 아이템의 특정 첨부파일 삭제
 * Actor: JOB_SEEKER
 */
@Operation(summary = "첨부파일 삭제", description = "포트폴리오 아이템의 특정 첨부파일을 삭제합니다.")
@DeleteMapping("/items/{itemId}/attachments")
public ResponseEntity<?> deleteAttachment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable String itemId,
        @RequestParam String objectKey
) {
    String userId = userDetails.getUsername();
    portfolioService.deleteAttachment(userId, itemId, objectKey);
    return ResponseEntity.noContent().build();
}
```

**2. Service Layer** (`PortfolioService.java`)
```java
/**
 * 포트폴리오 아이템의 특정 첨부파일 삭제
 */
@Transactional
public void deleteAttachment(String userId, String itemId, String objectKey) {
    log.info("Deleting attachment {} from item {} for userId: {}", objectKey, itemId, userId);

    // 포트폴리오 조회
    Portfolio portfolio = portfolioRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

    // 아이템 찾기
    PortfolioItem targetItem = portfolio.getPortfolioItems().stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오 아이템을 찾을 수 없습니다"));

    // 첨부파일 목록에서 해당 objectKey를 가진 파일 찾기
    List<Attachment> attachments = targetItem.getAttachments();
    if (attachments == null || attachments.isEmpty()) {
        throw new IllegalArgumentException("첨부파일이 존재하지 않습니다");
    }

    boolean removed = attachments.removeIf(attachment ->
        attachment.getObjectKey().equals(objectKey)
    );

    if (!removed) {
        throw new IllegalArgumentException("첨부파일을 찾을 수 없습니다");
    }

    // R2에서 실제 파일 삭제
    try {
        fileStorageService.deleteFile(objectKey);
        log.info("File deleted from R2: {}", objectKey);
    } catch (Exception e) {
        log.error("Failed to delete file from R2: {}", objectKey, e);
        // MongoDB는 이미 업데이트되었으므로 경고만 로깅
        log.warn("Attachment metadata removed from MongoDB but R2 deletion failed");
    }

    // Portfolio 업데이트
    targetItem.setUpdatedAt(LocalDateTime.now());
    portfolio.setUpdatedAt(LocalDateTime.now());

    // 재임베딩 플래그 설정 (선택사항 - 파일 내용이 임베딩에 포함되는 경우)
    portfolio.getProcessingStatus().setNeedsEmbedding(true);

    portfolioRepository.save(portfolio);
    log.info("Attachment deleted successfully: {}", objectKey);
}
```

**3. FileStorageService 확장**
`FileStorageService`에 단일 파일 삭제 메서드 추가 필요:
```java
/**
 * R2에서 단일 파일 삭제
 */
public void deleteFile(String objectKey) {
    // 기존 deleteFiles(List<String> objectKeys) 활용
    deleteFiles(Collections.singletonList(objectKey));
}
```

#### 비즈니스 로직

1. **인증 확인**: JWT 토큰으로 userId 추출
2. **포트폴리오 조회**: userId로 포트폴리오 존재 확인
3. **아이템 검증**: itemId로 해당 아이템이 포트폴리오에 속해있는지 확인
4. **첨부파일 검색**: objectKey로 해당 첨부파일 찾기
5. **MongoDB 업데이트**: Attachment 배열에서 제거
6. **R2 파일 삭제**: 실제 파일 삭제
7. **타임스탬프 갱신**: updatedAt 갱신 및 재임베딩 플래그 설정

#### 에러 처리

| 상황 | HTTP Status | 메시지 |
|------|-------------|--------|
| 포트폴리오 없음 | 400 Bad Request | 포트폴리오를 찾을 수 없습니다 |
| 아이템 없음 | 404 Not Found | 포트폴리오 아이템을 찾을 수 없습니다 |
| 첨부파일 없음 | 404 Not Found | 첨부파일을 찾을 수 없습니다 |
| objectKey 누락 | 400 Bad Request | objectKey 파라미터가 필요합니다 |
| 권한 없음 | 403 Forbidden | 해당 포트폴리오에 대한 권한이 없습니다 |

#### 고려사항

**1. 트랜잭션 일관성**
- MongoDB 업데이트와 R2 파일 삭제가 분리된 작업
- R2 삭제 실패 시에도 MongoDB는 업데이트됨 (orphaned file 발생 가능)
- 해결 방안: 주기적인 cleanup job으로 orphaned files 정리

**2. 동시성 제어**
- 여러 요청이 동시에 같은 파일을 삭제하려는 경우
- MongoDB의 원자적 업데이트로 해결됨
- R2 삭제는 멱등성(idempotent) 보장

**3. 파일 복구 불가**
- 삭제된 파일은 복구 불가능
- 향후 확장: Soft delete 또는 휴지통 기능 추가 고려

**4. 빈 첨부파일 배열**
- 마지막 첨부파일 삭제 후 빈 배열로 남음
- 문제 없음 (null이 아닌 빈 배열 유지)

**5. 임베딩 업데이트**
- 첨부파일에서 텍스트를 추출하여 임베딩에 포함하는 경우
- 파일 삭제 시 needsEmbedding = true 설정 필요
- 현재는 OCR 미구현 상태이므로 선택사항

#### 테스트 케이스

1. **정상 케이스**
   - 존재하는 첨부파일 삭제 → 204 No Content
   - 여러 첨부파일 중 하나만 삭제 → 나머지는 유지됨
   - 마지막 첨부파일 삭제 → 빈 배열로 남음

2. **에러 케이스**
   - 존재하지 않는 itemId → 404 Not Found
   - 존재하지 않는 objectKey → 404 Not Found
   - 다른 사용자의 포트폴리오 접근 시도 → 403 Forbidden
   - objectKey 파라미터 누락 → 400 Bad Request

3. **경계 케이스**
   - 아이템에 첨부파일이 하나도 없는 경우 → 404 Not Found
   - R2 삭제 실패 시 MongoDB는 업데이트됨 (경고 로그)

#### 대안: 인덱스 기반 삭제

objectKey 대신 배열 인덱스 사용:
```
DELETE /api/portfolios/items/{itemId}/attachments/{index}
```

**장점**: URL이 더 간결
**단점**:
- 프론트엔드에서 인덱스 관리 필요
- 동시 수정 시 인덱스 불일치 가능
- objectKey가 더 명확하고 안전함

**결론**: objectKey 사용 권장

#### 향후 확장

1. **일괄 삭제**: 여러 첨부파일을 한 번에 삭제
   ```
   DELETE /api/portfolios/items/{itemId}/attachments
   Body: { "objectKeys": ["key1", "key2"] }
   ```

2. **Soft Delete**: 즉시 삭제 대신 삭제 예정 표시
   - 30일 후 실제 삭제
   - 복구 기능 제공

3. **버전 관리**: 파일 교체 시 이전 버전 보관
   - 파일 히스토리 추적
   - 이전 버전으로 롤백 가능
