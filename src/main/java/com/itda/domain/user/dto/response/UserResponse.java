package com.itda.domain.user.dto.response;

import com.itda.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자 정보")
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String country,
        String language,
        @Schema(description = "자기소개")
        String bio,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "이니셜 (성의 첫 글자)", example = "K")
        String initial,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getLanguage(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getLastName().substring(0, 1).toUpperCase(),
                user.getCreatedAt()
        );
    }
}
