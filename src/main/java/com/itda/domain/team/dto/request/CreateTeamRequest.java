package com.itda.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(
        @NotBlank(message = "프로젝트 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "기본 언어를 선택해주세요.")
        String defaultLanguage
) {}
