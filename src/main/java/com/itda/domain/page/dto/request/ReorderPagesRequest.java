package com.itda.domain.page.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReorderPagesRequest(
        @NotEmpty(message = "페이지 순서를 입력해주세요.")
        List<Long> pageIds
) {}
