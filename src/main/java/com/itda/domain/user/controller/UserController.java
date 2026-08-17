package com.itda.domain.user.controller;

import com.itda.domain.page.dto.response.PresignedUrlResponse;
import com.itda.domain.user.dto.request.*;
import com.itda.domain.user.dto.response.UserResponse;
import com.itda.domain.user.service.UserService;
import com.itda.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 정보 조회 및 설정")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.ok("사용자 정보를 조회했습니다.", response));
    }

    @Operation(summary = "프로필 수정", description = "이름, 국적, 언어, 자기소개를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("프로필이 수정되었습니다.", response));
    }

    @Operation(summary = "프로필 이미지 업로드 URL 발급",
            description = "프로필 이미지 업로드를 위한 Presigned URL을 발급합니다.")
    @PostMapping("/me/profile-image/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getProfileImagePresignedUrl(
            Authentication authentication,
            @Valid @RequestBody ProfileImagePresignedUrlRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PresignedUrlResponse response = userService.generateProfileImagePresignedUrl(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Presigned URL이 발급되었습니다.", response));
    }

    @Operation(summary = "프로필 이미지 저장", description = "업로드 완료된 프로필 이미지 URL을 저장합니다.")
    @PutMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.updateProfileImage(userId, request.profileImageUrl());
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지가 수정되었습니다.", response));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다.")
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지가 삭제되었습니다.", null));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인 후 새 비밀번호로 변경합니다.")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다.", null));
    }

    @Operation(summary = "이메일 변경",
            description = "비밀번호 확인 후 이메일을 변경합니다. 기존 세션이 만료됩니다.")
    @PutMapping("/me/email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(
            Authentication authentication,
            @Valid @RequestBody ChangeEmailRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changeEmail(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("이메일이 변경되었습니다. 다시 로그인해주세요.", null));
    }

    @Operation(summary = "회원 탈퇴",
            description = "비밀번호 확인 후 계정을 삭제합니다. 되돌릴 수 없습니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAccount(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("계정이 삭제되었습니다.", null));
    }
}
