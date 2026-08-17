package com.itda.domain.requirement.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateRequirementRequest(
        @Size(max = 10, message = "항목명은 최대 10자입니다.")
        String itemName,

        String content,

        Boolean isRequired
) {}
