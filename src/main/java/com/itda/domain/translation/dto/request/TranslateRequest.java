package com.itda.domain.translation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TranslateRequest(
        @NotEmpty(message = "번역 대상을 선택해주세요.")
        @Valid
        List<TranslationTarget> translations
) {
    public record TranslationTarget(
            @NotNull(message = "사용자 ID는 필수입니다.")
            Long userId,
            @NotBlank(message = "번역 언어는 필수입니다.")
            String targetLanguage
    ) {}
}
