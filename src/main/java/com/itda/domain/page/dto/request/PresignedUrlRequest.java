package com.itda.domain.page.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @NotBlank(message = "파일명을 입력해주세요.")
        String fileName,

        @NotBlank(message = "Content-Type을 입력해주세요.")
        String contentType,

        @NotBlank(message = "이미지 타입을 선택해주세요.")
        String imageType,

        @NotNull(message = "페이지 ID를 입력해주세요.")
        Long pageId
) {}
