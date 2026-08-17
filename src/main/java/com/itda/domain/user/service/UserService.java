package com.itda.domain.user.service;

import com.itda.domain.auth.repository.RefreshTokenRepository;
import com.itda.domain.page.dto.response.PresignedUrlResponse;
import com.itda.domain.user.dto.request.*;
import com.itda.domain.user.dto.response.UserResponse;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.DuplicateException;
import com.itda.global.error.NotFoundException;
import com.itda.global.error.UnauthorizedException;
import com.itda.global.error.ValidationException;
import com.itda.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public UserResponse getMyInfo(Long userId) {
        User user = findUser(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.updateProfile(
                request.firstName(), request.lastName(),
                request.country(), request.language(), request.bio()
        );
        return UserResponse.from(user);
    }

    public PresignedUrlResponse generateProfileImagePresignedUrl(Long userId,
                                                                  ProfileImagePresignedUrlRequest request) {
        findUser(userId);
        String extension = extractExtension(request.fileName());
        String key = String.format("profiles/%d/%d.%s", userId, System.currentTimeMillis(), extension);
        String presignedUrl = s3Service.generatePresignedUploadUrl(key, request.contentType());
        String fileUrl = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(presignedUrl, fileUrl, key);
    }

    @Transactional
    public UserResponse updateProfileImage(Long userId, String profileImageUrl) {
        User user = findUser(userId);
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            String oldKey = s3Service.extractKeyFromUrl(user.getProfileImageUrl());
            s3Service.deleteFile(oldKey);
        }
        user.updateProfileImageUrl(profileImageUrl);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findUser(userId);
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            String oldKey = s3Service.extractKeyFromUrl(user.getProfileImageUrl());
            s3Service.deleteFile(oldKey);
            user.updateProfileImageUrl(null);
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ValidationException("새 비밀번호는 현재 비밀번호와 다르게 설정해주세요.");
        }
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void changeEmail(Long userId, ChangeEmailRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        if (user.getEmail().equals(request.newEmail())) {
            throw new ValidationException("현재 이메일과 동일합니다.");
        }
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }
        user.updateEmail(request.newEmail());
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            String key = s3Service.extractKeyFromUrl(user.getProfileImageUrl());
            s3Service.deleteFile(key);
        }
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "png";
        return fileName.substring(lastDot + 1);
    }
}
