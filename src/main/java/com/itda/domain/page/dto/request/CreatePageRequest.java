package com.itda.domain.page.dto.request;

import jakarta.validation.constraints.Size;

public record CreatePageRequest(
        @Size(max = 10, message = "화면 이름은 최대 10자입니다.")
        String screenName,

        @Size(max = 10, message = "화면 ID는 최대 10자입니다.")
        String screenId
) {}
