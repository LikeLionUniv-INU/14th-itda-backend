package com.itda.domain.requirement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequirementRequest(
        @NotBlank(message = "탭 유형을 선택해주세요.")
        String tabType,

        @Size(max = 10, message = "항목명은 최대 10자입니다.")
        String itemName,

        @Size(max = 200, message = "내용은 최대 200자입니다.")
        String content
) {}
