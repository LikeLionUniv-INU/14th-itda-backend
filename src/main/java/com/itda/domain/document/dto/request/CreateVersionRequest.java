package com.itda.domain.document.dto.request;

import jakarta.validation.constraints.Min;

public record CreateVersionRequest(
        @Min(value = 1, message = "기준 버전은 1 이상이어야 합니다.")
        int baseVersion
) {}
