package com.example.experfolio.domain.user.controller;

import com.example.experfolio.domain.user.dto.*;
import com.example.experfolio.domain.user.entity.User;
import com.example.experfolio.domain.user.entity.UserRole;
import com.example.experfolio.domain.user.service.AuthService;
import com.example.experfolio.domain.user.service.UserService;
import com.example.experfolio.global.exception.BadRequestException;
import com.example.experfolio.global.exception.UnauthorizedException;
import com.example.experfolio.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 사용자 관리 API 컨트롤러
 * 사용자 정보 조회, 수정, 삭제 등의 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "사용자 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 사용자 정보 조회
     */
    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인된 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserProfile(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("사용자 프로필 조회 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        UUID userId = authService.getUserIdFromToken(accessToken);
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));
        
        UserInfoResponseDto responseDto = convertToUserInfoResponse(user);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 프로필 조회가 완료되었습니다.", responseDto));
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/profile")
    @Operation(summary = "사용자 프로필 수정", description = "현재 로그인된 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> updateUserProfile(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUserRequestDto updateRequest) {
        
        log.info("사용자 프로필 수정 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        UUID userId = authService.getUserIdFromToken(accessToken);
        
        // 이메일 중복 검사 (다른 사용자가 이미 사용 중인지)
        if (updateRequest.getEmail() != null) {
            String currentEmail = authService.getUserEmailFromToken(accessToken);
            if (!currentEmail.equals(updateRequest.getEmail()) && 
                !userService.isEmailAvailable(updateRequest.getEmail())) {
                throw new BadRequestException("이미 사용 중인 이메일입니다: " + updateRequest.getEmail());
            }
        }
        
        // 사용자 정보 업데이트
        User updatedUser = userService.updateUserInfo(
            userId,
            updateRequest.getName(),
            updateRequest.getPhoneNumber()
        );
        
        // 이메일 업데이트 (별도 처리 - 재인증 필요)
        if (updateRequest.getEmail() != null) {
            updatedUser = userService.updateEmail(userId, updateRequest.getEmail());
        }
        
        UserInfoResponseDto responseDto = convertToUserInfoResponse(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 프로필이 수정되었습니다.", responseDto));
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequest) {
        
        log.info("비밀번호 변경 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        UUID userId = authService.getUserIdFromToken(accessToken);
        
        // 새 비밀번호 확인 검증
        if (!changePasswordRequest.isNewPasswordMatching()) {
            throw new BadRequestException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 비밀번호 변경
        authService.changePassword(
            userId,
            changePasswordRequest.getCurrentPassword(),
            changePasswordRequest.getNewPassword()
        );
        
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    /**
     * 계정 삭제 (탈퇴)
     */
    @DeleteMapping("/account")
    @Operation(summary = "계정 삭제", description = "현재 로그인된 사용자의 계정을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(description = "계정 삭제 확인을 위한 비밀번호", required = true)
            @RequestParam("password") String password) {
        
        log.info("계정 삭제 요청");
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        UUID userId = authService.getUserIdFromToken(accessToken);
        
        // 비밀번호 확인 후 계정 삭제
        userService.deleteUser(userId, password);
        
        return ResponseEntity.ok(ApiResponse.success("계정이 삭제되었습니다.", null));
    }

    /**
     * 특정 사용자 정보 조회 (관리자 또는 제한된 정보)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "특정 사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다. (제한된 정보만 제공)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserById(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID userId) {
        
        log.info("특정 사용자 정보 조회 요청: userId={}", userId);
        
        String accessToken = extractTokenFromHeader(authorizationHeader);
        UUID currentUserId = authService.getUserIdFromToken(accessToken);
        UserRole currentUserRole = getUserRoleFromToken(accessToken);
        
        // 자신의 정보이거나 관리자인 경우에만 조회 가능
        if (!currentUserId.equals(userId) && currentUserRole != UserRole.ADMIN) {
            throw new UnauthorizedException("다른 사용자의 정보를 조회할 권한이 없습니다.");
        }
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));
        
        UserInfoResponseDto responseDto = convertToUserInfoResponse(user);
        
        // 다른 사용자 정보 조회 시 민감한 정보 제한 (관리자가 아닌 경우)
        if (!currentUserId.equals(userId) && currentUserRole != UserRole.ADMIN) {
            responseDto = limitSensitiveInfo(responseDto);
        }
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", responseDto));
    }

    /**
     * 사용자 목록 조회 (관리자 전용)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    public ResponseEntity<ApiResponse<Object>> getAllUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("사용자 목록 조회 요청: page={}, size={}, sortBy={}, sortDirection={}", 
                page, size, sortBy, sortDirection);
        
        // 페이지네이션된 사용자 목록 조회 (추후 구현)
        // PagedResult<User> users = userService.findAllUsers(page, size, sortBy, sortDirection);
        
        // 현재는 간단한 구현으로 처리
        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 기능은 추후 구현됩니다.", null));
    }

    /**
     * 사용자 계정 활성화/비활성화 (관리자 전용)
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 계정 상태 변경", description = "사용자 계정을 활성화 또는 비활성화합니다. (관리자 전용)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 상태 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> updateUserStatus(
            @PathVariable UUID userId,
            @Parameter(description = "활성화: true, 비활성화: false")
            @RequestParam boolean activate) {
        
        log.info("사용자 계정 상태 변경 요청: userId={}, activate={}", userId, activate);
        
        User user;
        if (activate) {
            user = userService.activateUser(userId);
        } else {
            user = userService.suspendUser(userId);
        }
        
        UserInfoResponseDto responseDto = convertToUserInfoResponse(user);
        
        String message = activate ? "계정이 활성화되었습니다." : "계정이 비활성화되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, responseDto));
    }

    // ===== 유틸리티 메소드 =====

    /**
     * Authorization 헤더에서 토큰 추출
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadRequestException("유효하지 않은 Authorization 헤더입니다.");
        }
        return authorizationHeader.substring(7);
    }

    /**
     * 토큰에서 사용자 역할 추출
     */
    private UserRole getUserRoleFromToken(String accessToken) {
        User user = authService.getCurrentUser(accessToken);
        return user.getRole();
    }

    /**
     * User 엔티티를 UserInfoResponseDto로 변환
     */
    private UserInfoResponseDto convertToUserInfoResponse(User user) {
        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .profileCompletionRate(calculateProfileCompletionRate(user))
                .build();
    }

    /**
     * 민감한 정보 제한 (다른 사용자 정보 조회 시)
     */
    private UserInfoResponseDto limitSensitiveInfo(UserInfoResponseDto dto) {
        return UserInfoResponseDto.builder()
                .userId(dto.getUserId())
                .name(maskName(dto.getName())) // 이름 마스킹
                .role(dto.getRole())
                .status(dto.getStatus())
                .emailVerified(dto.isEmailVerified())
                .createdAt(dto.getCreatedAt())
                .profileCompletionRate(dto.getProfileCompletionRate())
                // 이메일, 전화번호 등 민감한 정보 제외
                .build();
    }

    /**
     * 이름 마스킹 처리
     */
    private String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 프로필 완성도 계산
     */
    private Integer calculateProfileCompletionRate(User user) {
        int completedFields = 0;
        int totalFields = 5;
        
        if (user.getEmail() != null) completedFields++;
        if (user.getName() != null) completedFields++;
        if (user.getPhoneNumber() != null) completedFields++;
        if (user.isEmailVerified()) completedFields++;
        
        // 역할별 프로필 존재 여부 (추후 구현)
        completedFields++;
        
        return (completedFields * 100) / totalFields;
    }
}