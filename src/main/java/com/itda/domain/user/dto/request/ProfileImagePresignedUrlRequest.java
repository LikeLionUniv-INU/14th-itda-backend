package com.itda.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로필 이미지 Presigned URL 발급 요청")
public record ProfileImagePresignedUrlRequest(
        @Schema(description = "파일명", example = "profile.png")
        @NotBlank(message = "파일명을 입력해주세요.")
        String fileName,

        @Schema(description = "Content-Type", example = "image/png")
        @NotBlank(message = "Content-Type을 입력해주세요.")
        String contentType
) {}
