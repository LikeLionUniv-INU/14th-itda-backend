package com.itda.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "이름은 영문자만 입력 가능합니다.")
        String firstName,

        @NotBlank(message = "성을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "성은 영문자만 입력 가능합니다.")
        String lastName,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$",
                message = "8~16자의 영문, 숫자 조합으로 입력해주세요.")
        String password,

        @NotBlank(message = "국적을 입력해주세요.")
        String country,

        @NotBlank(message = "언어를 선택해주세요.")
        String language
) {}
