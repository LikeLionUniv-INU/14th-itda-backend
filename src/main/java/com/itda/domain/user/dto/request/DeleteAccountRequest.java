package com.itda.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record DeleteAccountRequest(
        @Schema(description = "현재 비밀번호 (본인 확인용)")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
