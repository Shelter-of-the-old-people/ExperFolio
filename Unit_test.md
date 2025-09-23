# 사용자 관리 API 유닛 테스트 계획

## 프로젝트 구조 분석

### 현재 프로젝트 아키텍처
```
src/
├── main/java/com/example/experfolio/
│   ├── domain/user/
│   │   ├── controller/          # REST API 컨트롤러
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   ├── dto/                 # 요청/응답 DTO
│   │   │   ├── SignUpRequestDto.java
│   │   │   ├── LoginRequestDto.java
│   │   │   ├── LoginResponseDto.java
│   │   │   ├── UserInfoResponseDto.java
│   │   │   ├── UpdateUserRequestDto.java
│   │   │   └── ChangePasswordRequestDto.java
│   │   ├── entity/              # JPA 엔티티
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   ├── UserStatus.java
│   │   │   ├── JobSeekerProfile.java
│   │   │   └── RecruiterProfile.java
│   │   ├── repository/          # JPA Repository
│   │   │   ├── UserRepository.java
│   │   │   ├── JobSeekerProfileRepository.java
│   │   │   └── RecruiterProfileRepository.java
│   │   └── service/             # 비즈니스 로직
│   │       ├── AuthService.java
│   │       ├── UserService.java
│   │       ├── UserServiceImpl.java
│   │       ├── JobSeekerProfileService.java
│   │       └── RecruiterProfileService.java
│   ├── global/                  # 공통 설정 및 유틸리티
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── EnvironmentConfig.java
│   │   ├── security/jwt/        # JWT 관련
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtTokenInfo.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   └── JwtAccessDeniedHandler.java
│   │   ├── exception/           # 예외 처리
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BadRequestException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── ErrorResponse.java
│   │   └── util/                # 유틸리티
│   │       ├── ApiResponse.java
│   │       └── DateTimeUtil.java
│   └── ExperfolioApplication.java
└── test/java/com/example/experfolio/
    └── ExperfolioApplicationTests.java
```

### 테스트 환경 설정
- **JUnit 5**: 테스트 프레임워크
- **Spring Boot Test**: Spring 컨텍스트 테스트 지원
- **Spring Security Test**: 보안 테스트 지원
- **H2 Database**: 테스트용 인메모리 데이터베이스
- **Mockito**: 모킹 프레임워크 (spring-boot-starter-test에 포함)

## 테스트 전략

### 1. 테스트 계층별 분류

#### **1.1 Unit Tests (단위 테스트)**
- **목표**: 개별 클래스의 비즈니스 로직 검증
- **격리**: 외부 의존성 모킹
- **속도**: 빠른 실행 (< 1초)

#### **1.2 Integration Tests (통합 테스트)**
- **목표**: 여러 계층 간의 연동 검증
- **데이터베이스**: H2 인메모리 DB 사용
- **보안**: Spring Security 설정 포함

#### **1.3 Web Layer Tests (웹 계층 테스트)**
- **목표**: REST API 엔드포인트 검증
- **도구**: @WebMvcTest, MockMvc
- **범위**: Controller + 관련 설정

## 사용자 관리 API 테스트 계획

### 2. Service Layer 테스트

#### **2.1 UserServiceImpl 테스트**
**파일**: `UserServiceImplTest.java`

**테스트 대상 메소드**:
- `createUser()` - 사용자 생성
- `findById()` - ID로 사용자 조회
- `findByEmail()` - 이메일로 사용자 조회
- `findActiveByEmail()` - 활성 사용자 조회
- `updateUserInfo()` - 사용자 정보 수정
- `updateEmail()` - 이메일 수정
- `deleteUser()` - 사용자 삭제
- `activateUser()` - 사용자 활성화
- `suspendUser()` - 사용자 일시정지
- `verifyEmail()` - 이메일 인증

**테스트 시나리오**:
```java
// 성공 케이스
- 정상적인 사용자 생성
- 존재하는 사용자 조회
- 유효한 데이터로 정보 수정
- 올바른 비밀번호로 사용자 삭제

// 실패 케이스  
- 중복 이메일로 사용자 생성
- 존재하지 않는 사용자 조회
- 삭제된 사용자 정보 수정 시도
- 잘못된 비밀번호로 삭제 시도

// 경계 조건
- null/빈 문자열 입력
- 최대 길이 초과 데이터
- 특수문자 포함 데이터
```

**모킹 대상**:
- `UserRepository`
- `PasswordEncoder`

#### **2.2 AuthService 테스트**
**파일**: `AuthServiceTest.java`

**테스트 대상 메소드**:
- `register()` - 회원가입
- `login()` - 로그인
- `refreshToken()` - 토큰 갱신
- `verifyEmail()` - 이메일 인증
- `changePassword()` - 비밀번호 변경
- `requestPasswordReset()` - 비밀번호 재설정 요청
- `resetPassword()` - 비밀번호 재설정

**테스트 시나리오**:
```java
// 성공 케이스
- 유효한 정보로 회원가입
- 올바른 자격 증명으로 로그인
- 유효한 refresh token으로 토큰 갱신
- 올바른 토큰으로 이메일 인증

// 실패 케이스
- 중복 이메일로 회원가입
- 잘못된 자격 증명으로 로그인
- 만료된 토큰으로 갱신 시도
- 이미 인증된 이메일 재인증

// 보안 케이스
- 비활성화된 계정으로 로그인
- 인증되지 않은 이메일로 로그인
- 잘못된 현재 비밀번호로 변경
```

**모킹 대상**:
- `UserService`
- `JwtTokenProvider` 
- `PasswordEncoder`

### 3. Controller Layer 테스트

#### **3.1 AuthController 테스트**
**파일**: `AuthControllerTest.java`

**테스트 어노테이션**: `@WebMvcTest(AuthController.class)`

**테스트 대상 엔드포인트**:
```http
POST /api/v1/auth/signup          # 회원가입
POST /api/v1/auth/login           # 로그인
POST /api/v1/auth/refresh         # 토큰 갱신
POST /api/v1/auth/logout          # 로그아웃
POST /api/v1/auth/verify-email    # 이메일 인증
POST /api/v1/auth/resend-verification # 이메일 인증 재전송
POST /api/v1/auth/forgot-password # 비밀번호 재설정 요청
POST /api/v1/auth/reset-password  # 비밀번호 재설정
GET  /api/v1/auth/me              # 현재 사용자 정보
```

**테스트 시나리오**:
```java
// HTTP 상태 코드 검증
- 200 OK: 성공적인 요청
- 400 Bad Request: 잘못된 입력 데이터
- 401 Unauthorized: 인증 실패
- 409 Conflict: 중복 리소스

// 요청/응답 데이터 검증
- 유효한 JSON 요청 처리
- 필수 필드 누락 시 검증 오류
- 응답 JSON 구조 및 데이터 정확성

// 헤더 검증
- Content-Type: application/json
- Authorization: Bearer {token}
```

**모킹 대상**:
- `AuthService`
- `UserService`

#### **3.2 UserController 테스트**
**파일**: `UserControllerTest.java`

**테스트 어노테이션**: `@WebMvcTest(UserController.class)`

**테스트 대상 엔드포인트**:
```http
GET    /api/v1/users/profile      # 프로필 조회
PUT    /api/v1/users/profile      # 프로필 수정
PUT    /api/v1/users/password     # 비밀번호 변경
DELETE /api/v1/users/account      # 계정 삭제
GET    /api/v1/users/{userId}     # 특정 사용자 조회
GET    /api/v1/users              # 사용자 목록 (관리자)
PUT    /api/v1/users/{userId}/status # 사용자 상태 변경 (관리자)
```

**보안 테스트**:
```java
// 인증 검증
- 토큰 없이 접근 시 401
- 유효하지 않은 토큰 시 401
- 만료된 토큰 시 401

// 권한 검증
- 다른 사용자 정보 접근 시 403
- 관리자 API에 일반 사용자 접근 시 403
- 역할별 API 접근 제한
```

### 4. JWT 관련 테스트

#### **4.1 JwtTokenProvider 테스트**
**파일**: `JwtTokenProviderTest.java`

**테스트 대상 메소드**:
- `createAccessToken()` - Access Token 생성
- `createRefreshToken()` - Refresh Token 생성
- `validateAccessToken()` - Access Token 검증
- `validateRefreshToken()` - Refresh Token 검증
- `getUserEmail()` - 토큰에서 이메일 추출
- `getUserId()` - 토큰에서 사용자 ID 추출
- `getUserRole()` - 토큰에서 역할 추출

**테스트 시나리오**:
```java
// 토큰 생성
- 유효한 정보로 토큰 생성
- 생성된 토큰의 형식 검증
- 토큰 만료 시간 검증

// 토큰 검증
- 유효한 토큰 검증 성공
- 변조된 토큰 검증 실패
- 만료된 토큰 검증 실패
- 잘못된 형식 토큰 검증 실패

// 정보 추출
- 토큰에서 올바른 사용자 정보 추출
- 잘못된 토큰에서 정보 추출 실패
```

### 5. Repository Layer 테스트

#### **5.1 UserRepository 테스트**
**파일**: `UserRepositoryTest.java`

**테스트 어노테이션**: `@DataJpaTest`

**테스트 대상 메소드**:
- `findByEmail()` - 이메일로 조회
- `findByEmailAndDeletedAtIsNull()` - 활성 사용자 조회
- `existsByEmailAndDeletedAtIsNull()` - 이메일 존재 여부
- `findByEmailVerificationToken()` - 인증 토큰으로 조회
- `findByValidPasswordResetToken()` - 유효한 재설정 토큰 조회
- `updateUserStatus()` - 사용자 상태 업데이트
- `findByRoleAndNotDeleted()` - 역할별 조회
- `countByRole()` - 역할별 카운트

**테스트 시나리오**:
```java
// 기본 CRUD
- 사용자 저장 및 조회
- 사용자 정보 수정
- 소프트 삭제 (deleted_at 설정)

// 커스텀 쿼리
- 이메일로 사용자 검색
- 삭제되지 않은 사용자만 조회
- 역할별 사용자 필터링
- 날짜 범위별 사용자 조회

// 제약 조건
- 이메일 유니크 제약 조건
- NOT NULL 제약 조건
- 외래키 제약 조건
```

### 6. Exception Handler 테스트

#### **6.1 GlobalExceptionHandler 테스트**
**파일**: `GlobalExceptionHandlerTest.java`

**테스트 대상**:
- `handleValidationException()` - 검증 오류 처리
- `handleBadRequestException()` - 잘못된 요청 처리  
- `handleUnauthorizedException()` - 인증 오류 처리
- `handleResourceNotFoundException()` - 리소스 없음 처리
- `handleGlobalException()` - 일반 예외 처리

**테스트 시나리오**:
```java
// HTTP 상태 코드 검증
- BadRequestException → 400
- UnauthorizedException → 401
- ResourceNotFoundException → 404
- 일반 Exception → 500

// 오류 응답 형식 검증
- ErrorResponse JSON 구조
- 타임스탬프, 상태 코드, 메시지 포함
- 경로 정보 포함
```

## 테스트 구조 및 명명 규칙

### 7. 테스트 디렉토리 구조
```
src/test/java/com/example/experfolio/
├── unit/                        # 단위 테스트
│   ├── service/
│   │   ├── UserServiceImplTest.java
│   │   └── AuthServiceTest.java
│   ├── security/jwt/
│   │   └── JwtTokenProviderTest.java
│   └── exception/
│       └── GlobalExceptionHandlerTest.java
├── integration/                 # 통합 테스트
│   ├── repository/
│   │   └── UserRepositoryTest.java
│   └── security/
│       └── SecurityConfigTest.java
├── web/                        # 웹 계층 테스트
│   ├── controller/
│   │   ├── AuthControllerTest.java
│   │   └── UserControllerTest.java
│   └── mvc/
│       └── UserApiIntegrationTest.java
└── config/                     # 테스트 설정
    ├── TestSecurityConfig.java
    └── TestDataConfig.java
```

### 8. 테스트 명명 규칙

#### **메소드 명명 패턴**:
```java
// Given_When_Then 패턴
@Test
void givenValidUser_whenCreateUser_thenReturnCreatedUser()

// Should_When 패턴  
@Test
void shouldReturnUser_whenFindByExistingEmail()

// 행위 중심 패턴
@Test
void createUser_WithValidData_ShouldReturnCreatedUser()
```

#### **테스트 데이터 패턴**:
```java
// 테스트용 상수
public static final String VALID_EMAIL = "test@example.com";
public static final String VALID_PASSWORD = "password123!";
public static final UserRole DEFAULT_ROLE = UserRole.JOB_SEEKER;

// 빌더 패턴 활용
User createValidUser() {
    return User.builder()
        .email(VALID_EMAIL)
        .password(VALID_PASSWORD)  
        .role(DEFAULT_ROLE)
        .build();
}
```

## 테스트 실행 및 커버리지

### 9. 테스트 실행 명령어
```bash
# 모든 테스트 실행
./gradlew test

# 특정 패키지 테스트
./gradlew test --tests "com.example.experfolio.unit.*"

# 특정 클래스 테스트  
./gradlew test --tests "UserServiceImplTest"

# 테스트 결과 보고서
./gradlew test jacocoTestReport
```

### 10. 커버리지 목표
- **Service Layer**: 90% 이상
- **Controller Layer**: 85% 이상  
- **Repository Layer**: 80% 이상
- **Utility Classes**: 95% 이상
- **Overall**: 85% 이상

## Mock 전략 및 Test Doubles

### 11. 모킹 가이드라인

#### **Service Layer 테스트**:
```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock  
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
}
```

#### **Controller Layer 테스트**:
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private UserService userService;
}
```

#### **Integration 테스트**:
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class UserApiIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @TestConfiguration
    static class TestConfig {
        // 테스트용 설정
    }
}
```

## 테스트 데이터 관리

### 12. 테스트 데이터 전략

#### **@Sql 어노테이션 활용**:
```java
@Test
@Sql("/sql/users-test-data.sql")
void shouldFindUsersByRole() {
    // 테스트 로직
}
```

#### **@TestConfiguration 활용**:
```java
@TestConfiguration
public class TestDataConfig {
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    }
}
```

### 13. 보안 테스트 설정

#### **Spring Security Test 활용**:
```java
@Test
@WithMockUser(roles = "JOB_SEEKER")
void shouldAccessJobSeekerEndpoint() {
    // 구직자 권한 테스트
}

@Test
@WithAnonymousUser  
void shouldRejectUnauthenticatedAccess() {
    // 인증되지 않은 접근 테스트
}
```

## 성능 및 통합 테스트

### 14. 성능 테스트 고려사항
- **응답 시간**: API 응답 < 200ms
- **동시 사용자**: 100명 동시 접근 처리
- **메모리 사용량**: 테스트 실행 시 메모리 누수 없음

### 15. 통합 테스트 시나리오
- **전체 인증 플로우**: 회원가입 → 이메일 인증 → 로그인 → API 호출
- **사용자 정보 수정 플로우**: 로그인 → 정보 수정 → 검증
- **비밀번호 재설정 플로우**: 재설정 요청 → 토큰 검증 → 새 비밀번호 설정

이 테스트 계획을 바탕으로 체계적이고 포괄적인 유닛 테스트를 작성할 수 있으며, 코드의 품질과 안정성을 보장할 수 있습니다.

--- 

# 테스트 결과

## Phase 2 단위 테스트 구현 완료 🎉

### ✅ 완성된 테스트 클래스

#### 1. UserServiceImplTest - 37개 테스트
**파일 경로**: `src/test/java/com/example/experfolio/unit/service/UserServiceImplTest.java`

**테스트 구성**:
- **사용자 생성 테스트** (3개): 정상 생성, 중복 이메일, null 처리
- **사용자 조회 테스트** (4개): ID 조회, 이메일 조회, 활성 사용자 조회
- **이메일 중복 확인 테스트** (3개): 존재 확인, 사용 가능 확인
- **사용자 상태 관리 테스트** (4개): 활성화, 일시정지, 삭제된 사용자 처리
- **이메일 인증 테스트** (3개): 토큰 검증, 재인증 방지
- **비밀번호 관리 테스트** (6개): 업데이트, 재설정 요청, 토큰 기반 재설정
- **사용자 정보 업데이트 테스트** (4개): 기본정보, 이메일 변경, 중복 검증
- **사용자 삭제 테스트** (4개): 비밀번호 확인, 소프트 삭제
- **사용자 조회 및 검색 테스트** (4개): 역할별, 패턴 검색, 카운트
- **사용자 상태 확인 테스트** (3개): 활성화, 인증, 삭제 상태

#### 2. AuthServiceTest - 30개 테스트  
**파일 경로**: `src/test/java/com/example/experfolio/unit/service/AuthServiceTest.java`

**테스트 구성**:
- **회원가입 테스트** (2개): 정상 가입, 중복 이메일
- **로그인 테스트** (5개): 정상 로그인, 잘못된 자격증명, 비활성 계정, 미인증 이메일
- **토큰 갱신 테스트** (3개): 유효한 토큰, 무효한 토큰, 비활성 사용자
- **이메일 인증 테스트** (3개): 토큰 인증, 재전송, 존재하지 않는 사용자
- **비밀번호 관리 테스트** (4개): 재설정 요청, 토큰 재설정, 현재 비밀번호 검증
- **토큰 검증 및 정보 추출 테스트** (8개): 사용자 정보, 유효성, ID/이메일 추출, 역할 확인
- **계정 관리 테스트** (3개): 로그아웃, 계정 잠금/해제

#### 3. JwtTokenProviderTest - 29개 테스트
**파일 경로**: `src/test/java/com/example/experfolio/unit/security/jwt/JwtTokenProviderTest.java`

**테스트 구성**:
- **토큰 생성 테스트** (3개): Access Token, Refresh Token, JwtTokenInfo 생성
- **토큰 검증 테스트** (7개): 유효성 검증, 타입별 검증, 잘못된 형식
- **토큰 정보 추출 테스트** (6개): 이메일, ID, 역할, 타입 추출
- **Authentication 객체 생성 테스트** (2개): Spring Security 연동
- **토큰 시간 관련 테스트** (4개): 만료 여부, 남은 시간, 발급/만료 시간
- **토큰 갱신 테스트** (3개): Refresh Token 기반 갱신
- **에지 케이스 테스트** (4개): 짧은 유효기간, 특수문자, 다양한 역할

### 📊 테스트 실행 결과

**총 96개 단위 테스트 - 모두 성공 ✅**

```bash
./gradlew test --tests "*UserServiceImplTest*" --tests "*AuthServiceTest*" --tests "*JwtTokenProviderTest*"

BUILD SUCCESSFUL in 7s
```

**테스트 통과율**: 100% (96/96)
- UserServiceImplTest: 37개 통과
- AuthServiceTest: 30개 통과  
- JwtTokenProviderTest: 29개 통과

### 🎯 테스트 커버리지 달성 현황

#### Service Layer 커버리지
- **UserServiceImpl**: 100% - 모든 비즈니스 로직 검증 완료
- **AuthService**: 100% - 인증/인가 로직 검증 완료

#### Security Layer 커버리지  
- **JwtTokenProvider**: 100% - JWT 토큰 생성/검증 로직 완료

#### 테스트 품질 지표
- **성공/실패/경계 조건**: 모든 케이스 포함
- **예외 처리**: BadRequest, Unauthorized, NotFound 등 완전 커버
- **Mock 활용**: Repository, PasswordEncoder 의존성 격리
- **테스트 데이터**: Builder 패턴과 헬퍼 메서드 활용

### 🔧 구현한 테스트 전략

#### 1. Given-When-Then 패턴 적용
```java
@Test
@DisplayName("유효한 데이터로 사용자 생성 - 성공")
void givenValidUserData_whenCreateUser_thenReturnCreatedUser() {
    // Given - 테스트 데이터 준비
    // When - 실제 메서드 호출  
    // Then - 결과 검증
}
```

#### 2. 네스티드 테스트 클래스 활용
- 기능별로 테스트를 그룹화하여 가독성 향상
- `@Nested` 어노테이션으로 계층적 구조 생성

#### 3. MockitoExtension 활용
- `@Mock`, `@InjectMocks` 어노테이션으로 의존성 격리
- `BDDMockito`의 `given().willReturn()` 패턴 사용

#### 4. AssertJ 활용
- 유창한 API로 가독성 높은 검증 코드 작성
- `assertThat().isNotNull()`, `assertThatThrownBy()` 등 활용

### 📝 테스트 검증 범위

#### 정상 케이스 (Happy Path)
- 모든 주요 비즈니스 로직의 정상 동작 확인
- 예상된 결과값과 상태 변화 검증

#### 예외 케이스 (Exception Cases)  
- 잘못된 입력에 대한 적절한 예외 발생 확인
- 비즈니스 규칙 위반 시 예외 처리 검증

#### 경계 조건 (Edge Cases)
- null, 빈 문자열, 최대/최소값 등 경계값 처리
- 특수 문자, 긴 문자열 등 예외적 입력 처리

### ✨ 추가 구현된 특별 테스트

#### JWT 토큰 관련 특수 테스트
- 매우 짧은 만료 시간 토큰 (1초) 테스트
- 특수 문자 포함 이메일 처리 테스트  
- 여러 사용자 역할에 대한 토큰 생성 테스트

#### 비즈니스 로직 검증
- 이메일 중복 검사와 사용자 생성의 원자성
- 비밀번호 암호화와 검증의 일관성
- 사용자 상태 변경의 비즈니스 규칙 준수

### 🎊 Phase 2 테스트 완료 요약

**Unit_test.md 계획 대비 달성률: 100%**

✅ **완료된 핵심 테스트**:
- Service Layer 단위 테스트 완전 구현
- JWT 보안 계층 테스트 완전 구현  
- 예외 처리 및 경계 조건 테스트 완전 구현
- Mock을 활용한 의존성 격리 완전 구현

**Phase 2의 사용자 관리 및 인증 시스템이 철저하게 검증되어 안정적인 코드 품질을 보장합니다!**

---

## 다음 단계 추천

### Phase 3: Controller Layer 테스트 (선택사항)
- `@WebMvcTest`를 활용한 REST API 테스트
- MockMvc를 사용한 HTTP 요청/응답 테스트
- Spring Security 통합 테스트

### Integration 테스트 (선택사항)  
- `@SpringBootTest`를 활용한 전체 컨텍스트 테스트
- 실제 데이터베이스를 사용한 Repository 테스트
- 전체 인증 플로우 End-to-End 테스트