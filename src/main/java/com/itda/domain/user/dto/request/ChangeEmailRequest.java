package com.itda.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 변경 요청")
public record ChangeEmailRequest(
        @Schema(description = "현재 비밀번호 (본인 확인용)")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password,

        @Schema(description = "새 이메일", example = "newemail@example.com")
        @NotBlank(message = "새 이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String newEmail
) {}
