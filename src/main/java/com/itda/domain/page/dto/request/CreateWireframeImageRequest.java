package com.itda.domain.page.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWireframeImageRequest(
        @NotBlank(message = "이미지 타입을 선택해주세요.")
        String imageType,

        @NotBlank(message = "이미지 URL을 입력해주세요.")
        String imageUrl,

        Integer originalWidth,
        Integer originalHeight,

        @NotNull(message = "표시 너비를 입력해주세요.")
        Integer displayWidth,

        @NotNull(message = "표시 높이를 입력해주세요.")
        Integer displayHeight
) {}
