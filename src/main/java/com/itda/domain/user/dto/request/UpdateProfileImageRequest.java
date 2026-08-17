package com.itda.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로필 이미지 저장 요청")
public record UpdateProfileImageRequest(
        @Schema(description = "업로드된 프로필 이미지 URL")
        @NotBlank(message = "프로필 이미지 URL을 입력해주세요.")
        String profileImageUrl
) {}
