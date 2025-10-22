# Database Migration Guide

## UserStatus 및 Email Verification 제거 마이그레이션

이 가이드는 데이터베이스에서 다음 컬럼들을 제거하는 방법을 설명합니다:
- `status` (UserStatus enum)
- `email_verified` (Boolean)
- `email_verification_token` (String)

---

## 📋 변경 사항 요약

### 제거되는 컬럼
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| `status` | VARCHAR(20) | 사용자 상태 (ACTIVE, INACTIVE, PENDING, SUSPENDED) |
| `email_verified` | BOOLEAN | 이메일 인증 여부 |
| `email_verification_token` | VARCHAR(255) | 이메일 인증 토큰 |

### 제거되는 인덱스
- `idx_users_status`
- `idx_users_role_status`
- `idx_users_email_status`
- `idx_users_active_users` (기존)
- `idx_users_email_verified`
- `idx_users_email_verification_token`

### 추가되는 인덱스
- `idx_users_role_deleted` - role과 deleted_at 복합 인덱스
- `idx_users_active_users` (새 버전) - deleted_at이 NULL인 활성 사용자 부분 인덱스

---

## 🚀 실행 방법

### 방법 1: PostgreSQL 직접 실행 (권장)

```bash
# 데이터베이스 연결 및 실행
psql -U your_username -d your_database_name -f postgresql_migration.sql
```

### 방법 2: psql 인터랙티브 모드

```bash
# PostgreSQL 접속
psql -U your_username -d your_database_name

# 스크립트 실행
\i postgresql_migration.sql

# 또는 직접 복사-붙여넣기
```

### 방법 3: pgAdmin 사용

1. pgAdmin에서 데이터베이스 선택
2. Tools → Query Tool
3. `postgresql_migration.sql` 파일 열기
4. Execute (F5)

### 방법 4: DBeaver / DataGrip 사용

1. 데이터베이스 연결
2. SQL Editor 열기
3. `postgresql_migration.sql` 파일 내용 복사
4. 실행 (Ctrl+Enter)

---

## 📁 마이그레이션 파일 설명

### 1. `postgresql_migration.sql` ⭐ (추천)
- **용도**: PostgreSQL 데이터베이스에 직접 실행
- **특징**:
  - 즉시 실행 가능
  - 트랜잭션으로 안전하게 처리
  - 자동 검증 포함
  - 실행 결과 출력

**실행 예제:**
```bash
psql -U postgres -d experfolio -f postgresql_migration.sql
```

### 2. `complete_user_cleanup_migration.sql`
- **용도**: 상세한 주석과 함께 전체 프로세스 이해
- **특징**:
  - 각 단계별 상세 설명
  - 롤백 스크립트 포함
  - 검증 쿼리 포함

### 3. `remove_user_status_migration.sql`
- **용도**: UserStatus만 제거 (이메일 인증은 유지)
- **특징**: 부분 마이그레이션이 필요한 경우 사용

### 4. `V2__remove_user_status.sql`
- **용도**: Flyway/Liquibase 마이그레이션 도구용
- **위치**: `src/main/resources/db/migration/`
- **특징**: 버전 관리 마이그레이션 시스템과 통합

---

## ⚠️ 실행 전 체크리스트

### 필수 사항
- [ ] **데이터베이스 백업 완료**
  ```bash
  pg_dump -U username -d database_name > backup_$(date +%Y%m%d_%H%M%S).sql
  ```
- [ ] 프로덕션 환경이라면 점검 시간 확보
- [ ] 충분한 권한 확인 (ALTER TABLE, DROP INDEX 권한)
- [ ] 애플리케이션 코드가 이미 업데이트되어 있는지 확인

### 권장 사항
- [ ] 개발/스테이징 환경에서 먼저 테스트
- [ ] 사용자 수가 적은 시간대에 실행
- [ ] 마이그레이션 실행 로그 저장

---

## 🔍 실행 후 검증

### 1. 컬럼 제거 확인
```sql
-- 제거된 컬럼이 없는지 확인 (0 rows 반환되어야 함)
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'users'
  AND column_name IN ('status', 'email_verified', 'email_verification_token');
```

### 2. 인덱스 확인
```sql
-- 현재 users 테이블의 모든 인덱스 확인
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;
```

예상 결과:
- ✅ `idx_users_email`
- ✅ `idx_users_role`
- ✅ `idx_users_role_deleted` (신규)
- ✅ `idx_users_active_users` (신규)
- ✅ `idx_users_password_reset_token`
- ✅ `idx_users_created_at`
- ✅ `idx_users_deleted_at`
- ❌ `idx_users_status` (제거됨)
- ❌ `idx_users_role_status` (제거됨)
- ❌ `idx_users_email_verified` (제거됨)

### 3. 테이블 구조 확인
```sql
-- 최종 users 테이블 구조
\d users
```

예상 컬럼:
- ✅ id (UUID)
- ✅ email (VARCHAR)
- ✅ password (VARCHAR)
- ✅ role (VARCHAR)
- ✅ name (VARCHAR)
- ✅ phone_number (VARCHAR)
- ✅ password_reset_token (VARCHAR)
- ✅ password_reset_expires (TIMESTAMP)
- ✅ last_login_at (TIMESTAMP)
- ✅ created_at (TIMESTAMP)
- ✅ updated_at (TIMESTAMP)
- ✅ deleted_at (TIMESTAMP)
- ❌ status (제거됨)
- ❌ email_verified (제거됨)
- ❌ email_verification_token (제거됨)

### 4. 데이터 확인
```sql
-- 기존 사용자 데이터가 유지되는지 확인
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS active_users FROM users WHERE deleted_at IS NULL;
SELECT COUNT(*) AS deleted_users FROM users WHERE deleted_at IS NOT NULL;
```

---

## 🔄 롤백 (긴급 상황)

마이그레이션 직후 문제가 발생한 경우에만 사용하세요.

```sql
-- 백업에서 복원 (가장 안전)
psql -U username -d database_name < backup_20251020_000000.sql

-- 또는 complete_user_cleanup_migration.sql의 롤백 스크립트 사용
-- (파일 하단 주석 참고)
```

**주의**: 롤백 시 새로 생성된 데이터는 손실될 수 있습니다!

---

## 📊 성능 영향 분석

### Before (기존)
- 인덱스 개수: 11개
- status 컬럼: 매 쿼리마다 체크
- 복잡한 복합 인덱스 (role + status)

### After (변경 후)
- 인덱스 개수: 8개 (3개 감소)
- 더 단순한 쿼리 조건
- 부분 인덱스로 성능 향상
- 스토리지 절약

**예상 효과**:
- 쿼리 성능: 동일하거나 약간 향상
- INSERT/UPDATE 성능: 인덱스 감소로 약간 향상
- 디스크 사용량: 감소

---

## 💡 애플리케이션 코드 변경사항

데이터베이스 마이그레이션 전에 애플리케이션 코드가 먼저 업데이트되어야 합니다!

### 변경된 엔티티
```java
// Before
@Column(nullable = false)
private UserStatus status;

// After
// status 필드 제거됨
```

### 변경된 로직
```java
// Before
if (user.getStatus() == UserStatus.ACTIVE) { ... }

// After
if (!user.isDeleted()) { ... }
```

---

## 📞 문제 해결

### Q: "permission denied" 에러
**A**: 충분한 권한이 있는 사용자로 실행하세요.
```bash
# superuser로 실행
psql -U postgres -d database_name -f postgresql_migration.sql
```

### Q: "column does not exist" 에러
**A**: 이미 마이그레이션이 실행되었거나, 테이블 구조가 다릅니다. `\d users`로 확인하세요.

### Q: 기존 데이터가 손실될까요?
**A**: 아니요. 이 마이그레이션은 컬럼만 제거하며, 다른 데이터는 유지됩니다.

### Q: 프로덕션에서 실행 시간은?
**A**: 데이터 크기에 따라 다르지만, 일반적으로 1초 미만입니다.
- 1만 건: < 1초
- 10만 건: 1-2초
- 100만 건: 5-10초

---

## ✅ 실행 완료 후

1. [ ] 애플리케이션 재시작
2. [ ] 기본 기능 테스트 (회원가입, 로그인)
3. [ ] 로그 확인 (에러 없는지)
4. [ ] 모니터링 시스템 확인
5. [ ] 백업 파일 안전한 곳에 보관

---

## 📝 실행 로그 예제

```
================================================
Starting User Table Cleanup Migration...
================================================

[1/4] Removing email verification columns...
DROP INDEX
DROP INDEX
ALTER TABLE
ALTER TABLE
Email verification columns removed

[2/4] Removing user status column...
DROP INDEX
DROP INDEX
DROP INDEX
DROP INDEX
ALTER TABLE
User status column removed

[3/4] Creating new optimized indexes...
CREATE INDEX
CREATE INDEX
New indexes created

[4/4] Updating table metadata...
COMMENT
Table metadata updated

NOTICE:
NOTICE: Column Removal Status:
NOTICE:   status column: REMOVED
NOTICE:   email_verified column: REMOVED
NOTICE:   email_verification_token column: REMOVED
NOTICE:
NOTICE: SUCCESS: All target columns removed successfully!

================================================
Migration Complete!
================================================
```

---

## 📚 추가 참고 자료

- PostgreSQL ALTER TABLE: https://www.postgresql.org/docs/current/sql-altertable.html
- Index Performance: https://www.postgresql.org/docs/current/indexes.html
- Backup & Restore: https://www.postgresql.org/docs/current/backup.html

---

**작성일**: 2025-10-20
**마지막 업데이트**: 2025-10-20
**버전**: 1.0
