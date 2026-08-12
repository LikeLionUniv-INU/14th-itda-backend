package com.itda.domain.document.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(
        @NotBlank(message = "문서 이름을 입력해주세요.")
        @Size(max = 10, message = "문서 이름은 최대 10자입니다.")
        String name,

        @NotBlank(message = "언어를 선택해주세요.")
        String language,

        @Min(value = 1, message = "버전은 1 이상이어야 합니다.")
        int version,

        String documentType
) {}
