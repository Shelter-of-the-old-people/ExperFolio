package com.example.experfolio.unit.service;

import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.entity.UserStatus;
import com.example.experfolio.domain.user.repository.UserRepository;
import com.example.experfolio.domain.user.service.UserServiceImpl;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 단위 테스트")
class UserServiceImplTest {

    // 테스트용 상수
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final UserRole DEFAULT_ROLE = UserRole.JOB_SEEKER;
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String VERIFICATION_TOKEN = "test-verification-token";
    private static final String RESET_TOKEN = "test-reset-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createValidUser();
    }

    @Nested
    @DisplayName("사용자 생성 테스트")
    class CreateUserTests {

        @Test
        @DisplayName("유효한 데이터로 사용자 생성 - 성공")
        void givenValidUserData_whenCreateUser_thenReturnCreatedUser() {
            // Given
            User savedUser = User.builder()
                    .email(VALID_EMAIL)
                    .password(ENCODED_PASSWORD)
                    .role(DEFAULT_ROLE)
                    .build();
            savedUser.setEmailVerificationToken("some-token");
            
            given(userRepository.existsByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // When
            User createdUser = userService.createUser(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE);

            // Then
            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(createdUser.getRole()).isEqualTo(DEFAULT_ROLE);
            assertThat(createdUser.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(createdUser.getEmailVerificationToken()).isNotNull();

            verify(passwordEncoder).encode(VALID_PASSWORD);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("중복 이메일로 사용자 생성 - 실패")
        void givenDuplicateEmail_whenCreateUser_thenThrowBadRequestException() {
            // Given
            given(userRepository.existsByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(VALID_EMAIL, VALID_PASSWORD, DEFAULT_ROLE))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("null 이메일로 사용자 생성 - 실패")
        void givenNullEmail_whenCreateUser_thenHandleGracefully() {
            // Given
            String nullEmail = null;
            
            // When & Then
            assertThatThrownBy(() -> userService.createUser(nullEmail, VALID_PASSWORD, DEFAULT_ROLE))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class FindUserTests {

        @Test
        @DisplayName("ID로 사용자 조회 - 성공")
        void givenExistingUserId_whenFindById_thenReturnUser() {
            // Given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findById(TEST_USER_ID);

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 사용자 조회 - 빈 결과")
        void givenNonExistentUserId_whenFindById_thenReturnEmpty() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When
            Optional<User> foundUser = userService.findById(nonExistentId);

            // Then
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("이메일로 사용자 조회 - 성공")
        void givenExistingEmail_whenFindByEmail_thenReturnUser() {
            // Given
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findByEmail(VALID_EMAIL);

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(VALID_EMAIL);
        }

        @Test
        @DisplayName("활성 사용자 이메일로 조회 - 성공")
        void givenActiveUserEmail_whenFindActiveByEmail_thenReturnUser() {
            // Given
            given(userRepository.findByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(Optional.of(testUser));

            // When
            Optional<User> foundUser = userService.findActiveByEmail(VALID_EMAIL);

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(VALID_EMAIL);
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인 테스트")
    class EmailValidationTests {

        @Test
        @DisplayName("존재하는 이메일 확인 - true 반환")
        void givenExistingEmail_whenExistsByEmail_thenReturnTrue() {
            // Given
            given(userRepository.existsByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(true);

            // When
            boolean exists = userService.existsByEmail(VALID_EMAIL);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("사용 가능한 이메일 확인 - true 반환")
        void givenAvailableEmail_whenIsEmailAvailable_thenReturnTrue() {
            // Given
            given(userRepository.existsByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(false);

            // When
            boolean available = userService.isEmailAvailable(VALID_EMAIL);

            // Then
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("이미 사용 중인 이메일 확인 - false 반환")
        void givenUsedEmail_whenIsEmailAvailable_thenReturnFalse() {
            // Given
            given(userRepository.existsByEmailAndDeletedAtIsNull(VALID_EMAIL)).willReturn(true);

            // When
            boolean available = userService.isEmailAvailable(VALID_EMAIL);

            // Then
            assertThat(available).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 상태 관리 테스트")
    class UserStatusTests {

        @Test
        @DisplayName("사용자 활성화 - 성공")
        void givenValidUserId_whenActivateUser_thenReturnActivatedUser() {
            // Given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            
            // When
            User activatedUser = userService.activateUser(TEST_USER_ID);

            // Then
            assertThat(activatedUser).isNotNull();
            verify(userRepository).updateUserStatus(TEST_USER_ID, UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 활성화 - 실패")
        void givenNonExistentUserId_whenActivateUser_thenThrowResourceNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.activateUser(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("사용자 일시정지 - 성공")
        void givenValidUserId_whenSuspendUser_thenReturnSuspendedUser() {
            // Given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // When
            User suspendedUser = userService.suspendUser(TEST_USER_ID);

            // Then
            assertThat(suspendedUser).isNotNull();
            verify(userRepository).updateUserStatus(TEST_USER_ID, UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("삭제된 사용자 상태 변경 시도 - 실패")
        void givenDeletedUser_whenUpdateUserStatus_thenThrowBadRequestException() {
            // Given
            User deletedUser = createValidUser();
            deletedUser.softDelete();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(deletedUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUserStatus(TEST_USER_ID, UserStatus.ACTIVE))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("삭제된 사용자의 상태를 변경할 수 없습니다");

            verify(userRepository, never()).updateUserStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("이메일 인증 테스트")
    class EmailVerificationTests {

        @Test
        @DisplayName("유효한 토큰으로 이메일 인증 - 성공")
        void givenValidToken_whenVerifyEmail_thenVerifyEmailSuccessfully() {
            // Given
            testUser.setEmailVerificationToken(VERIFICATION_TOKEN);
            given(userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN))
                    .willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.verifyEmail(VERIFICATION_TOKEN);

            // Then
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 이메일 인증 - 실패")
        void givenInvalidToken_whenVerifyEmail_thenThrowBadRequestException() {
            // Given
            String invalidToken = "invalid-token";
            given(userRepository.findByEmailVerificationToken(invalidToken))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.verifyEmail(invalidToken))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("유효하지 않은 인증 토큰입니다");
        }

        @Test
        @DisplayName("이미 인증된 이메일 재인증 시도 - 실패")
        void givenAlreadyVerifiedEmail_whenVerifyEmail_thenThrowBadRequestException() {
            // Given
            testUser.setEmailVerificationToken(VERIFICATION_TOKEN);
            testUser.verifyEmail(); // 이미 인증된 상태로 설정
            given(userRepository.findByEmailVerificationToken(VERIFICATION_TOKEN))
                    .willReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.verifyEmail(VERIFICATION_TOKEN))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 인증된 이메일입니다");
        }
    }

    @Nested
    @DisplayName("비밀번호 관리 테스트")
    class PasswordManagementTests {

        @Test
        @DisplayName("비밀번호 업데이트 - 성공")
        void givenValidUserIdAndPassword_whenUpdatePassword_thenUpdateSuccessfully() {
            // Given
            String newPassword = "newPassword123!";
            String encodedNewPassword = "encoded-new-password";
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.updatePassword(TEST_USER_ID, newPassword);

            // Then
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("비밀번호 재설정 요청 - 성공")
        void givenActiveUserEmail_whenRequestPasswordReset_thenGenerateResetToken() {
            // Given
            given(userRepository.findByEmailAndDeletedAtIsNull(VALID_EMAIL))
                    .willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.requestPasswordReset(VALID_EMAIL);

            // Then
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 요청 - 실패")
        void givenNonExistentEmail_whenRequestPasswordReset_thenThrowResourceNotFoundException() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";
            given(userRepository.findByEmailAndDeletedAtIsNull(nonExistentEmail))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.requestPasswordReset(nonExistentEmail))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("유효한 토큰으로 비밀번호 재설정 - 성공")
        void givenValidResetToken_whenResetPassword_thenResetSuccessfully() {
            // Given
            String newPassword = "newPassword123!";
            String encodedNewPassword = "encoded-new-password";
            given(userRepository.findByValidPasswordResetToken(eq(RESET_TOKEN), any(LocalDateTime.class)))
                    .willReturn(Optional.of(testUser));
            given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.resetPassword(RESET_TOKEN, newPassword);

            // Then
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("만료된 토큰으로 비밀번호 재설정 - 실패")
        void givenExpiredResetToken_whenResetPassword_thenThrowBadRequestException() {
            // Given
            String newPassword = "newPassword123!";
            given(userRepository.findByValidPasswordResetToken(eq(RESET_TOKEN), any(LocalDateTime.class)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword(RESET_TOKEN, newPassword))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("유효하지 않거나 만료된 재설정 토큰입니다");
        }
    }

    @Nested
    @DisplayName("사용자 정보 업데이트 테스트")
    class UserInfoUpdateTests {

        @Test
        @DisplayName("사용자 기본 정보 업데이트 - 성공")
        void givenValidUserInfo_whenUpdateUserInfo_thenUpdateSuccessfully() {
            // Given
            String newName = "새로운 이름";
            String newPhoneNumber = "010-1234-5678";
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            User updatedUser = userService.updateUserInfo(TEST_USER_ID, newName, newPhoneNumber);

            // Then
            assertThat(updatedUser).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("이메일 업데이트 - 성공")
        void givenNewEmail_whenUpdateEmail_thenUpdateSuccessfully() {
            // Given
            String newEmail = "newemail@example.com";
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByEmailAndDeletedAtIsNull(newEmail)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            User updatedUser = userService.updateEmail(TEST_USER_ID, newEmail);

            // Then
            assertThat(updatedUser).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("중복된 이메일로 업데이트 시도 - 실패")
        void givenDuplicateEmail_whenUpdateEmail_thenThrowBadRequestException() {
            // Given
            String duplicateEmail = "duplicate@example.com";
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByEmailAndDeletedAtIsNull(duplicateEmail)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.updateEmail(TEST_USER_ID, duplicateEmail))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");
        }

        @Test
        @DisplayName("삭제된 사용자 정보 업데이트 시도 - 실패")
        void givenDeletedUser_whenUpdateUserInfo_thenThrowBadRequestException() {
            // Given
            User deletedUser = createValidUser();
            deletedUser.softDelete();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(deletedUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUserInfo(TEST_USER_ID, "새이름", "010-1234-5678"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("삭제된 사용자의 정보를 변경할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class UserDeletionTests {

        @Test
        @DisplayName("올바른 비밀번호로 사용자 삭제 - 성공")
        void givenCorrectPassword_whenDeleteUser_thenDeleteSuccessfully() {
            // Given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(VALID_PASSWORD, testUser.getPassword())).willReturn(true);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.deleteUser(TEST_USER_ID, VALID_PASSWORD);

            // Then
            verify(passwordEncoder).matches(VALID_PASSWORD, testUser.getPassword());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 사용자 삭제 시도 - 실패")
        void givenIncorrectPassword_whenDeleteUser_thenThrowBadRequestException() {
            // Given
            String incorrectPassword = "wrongpassword";
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(incorrectPassword, testUser.getPassword())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(TEST_USER_ID, incorrectPassword))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("비밀번호가 올바르지 않습니다");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 삭제된 사용자 삭제 시도 - 실패")
        void givenAlreadyDeletedUser_whenDeleteUser_thenThrowBadRequestException() {
            // Given
            User deletedUser = createValidUser();
            deletedUser.softDelete();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(deletedUser));

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(TEST_USER_ID, VALID_PASSWORD))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("이미 삭제된 사용자입니다");
        }

        @Test
        @DisplayName("소프트 삭제 - 성공")
        void givenValidUserId_whenSoftDeleteUser_thenDeleteSuccessfully() {
            // Given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When
            userService.softDeleteUser(TEST_USER_ID);

            // Then
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("사용자 조회 및 검색 테스트")
    class UserQueryTests {

        @Test
        @DisplayName("역할별 사용자 조회 - 성공")
        void givenUserRole_whenFindByRole_thenReturnUserList() {
            // Given
            List<User> expectedUsers = Arrays.asList(testUser);
            given(userRepository.findByRole(DEFAULT_ROLE)).willReturn(expectedUsers);

            // When
            List<User> foundUsers = userService.findByRole(DEFAULT_ROLE);

            // Then
            assertThat(foundUsers).hasSize(1);
            assertThat(foundUsers.get(0).getRole()).isEqualTo(DEFAULT_ROLE);
        }

        @Test
        @DisplayName("활성 사용자 조회 - 성공")
        void whenFindAllActiveUsers_thenReturnActiveUserList() {
            // Given
            List<User> expectedUsers = Arrays.asList(testUser);
            given(userRepository.findAllActiveUsersOrderByCreatedAtDesc()).willReturn(expectedUsers);

            // When
            List<User> foundUsers = userService.findAllActiveUsers();

            // Then
            assertThat(foundUsers).hasSize(1);
            assertThat(foundUsers.get(0)).isEqualTo(testUser);
        }

        @Test
        @DisplayName("역할별 사용자 수 조회 - 성공")
        void givenUserRole_whenCountUsersByRole_thenReturnCount() {
            // Given
            long expectedCount = 5L;
            given(userRepository.countActiveUsersByRole(DEFAULT_ROLE)).willReturn(expectedCount);

            // When
            long actualCount = userService.countUsersByRole(DEFAULT_ROLE);

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("이메일 패턴으로 사용자 검색 - 성공")
        void givenEmailPattern_whenSearchUsersByEmail_thenReturnMatchingUsers() {
            // Given
            String emailPattern = "test";
            List<User> expectedUsers = Arrays.asList(testUser);
            given(userRepository.findByEmailContainingAndNotDeleted(emailPattern)).willReturn(expectedUsers);

            // When
            List<User> foundUsers = userService.searchUsersByEmail(emailPattern);

            // Then
            assertThat(foundUsers).hasSize(1);
            assertThat(foundUsers.get(0).getEmail()).contains(emailPattern);
        }
    }

    @Nested
    @DisplayName("사용자 상태 확인 테스트")
    class UserStatusCheckTests {

        @Test
        @DisplayName("활성 사용자 상태 확인 - true")
        void givenActiveUser_whenIsUserActive_thenReturnTrue() {
            // Given
            testUser.activate();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // When
            boolean isActive = userService.isUserActive(TEST_USER_ID);

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("이메일 인증 상태 확인 - true")
        void givenVerifiedUser_whenIsEmailVerified_thenReturnTrue() {
            // Given
            testUser.verifyEmail();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // When
            boolean isVerified = userService.isEmailVerified(TEST_USER_ID);

            // Then
            assertThat(isVerified).isTrue();
        }

        @Test
        @DisplayName("삭제된 사용자 상태 확인 - true")
        void givenDeletedUser_whenIsUserDeleted_thenReturnTrue() {
            // Given
            testUser.softDelete();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // When
            boolean isDeleted = userService.isUserDeleted(TEST_USER_ID);

            // Then
            assertThat(isDeleted).isTrue();
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private User createValidUser() {
        return User.builder()
                .email(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .role(DEFAULT_ROLE)
                .build();
    }
}