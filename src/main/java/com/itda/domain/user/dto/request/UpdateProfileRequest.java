package com.itda.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
        @Schema(description = "이름 (영문)", example = "Seoyeon")
        @NotBlank(message = "이름을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "이름은 영문자만 입력 가능합니다.")
        String firstName,

        @Schema(description = "성 (영문)", example = "Kim")
        @NotBlank(message = "성을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "성은 영문자만 입력 가능합니다.")
        String lastName,

        @Schema(description = "국적", example = "대한민국")
        @NotBlank(message = "국적을 입력해주세요.")
        String country,

        @Schema(description = "사용 언어", example = "한국어")
        @NotBlank(message = "언어를 선택해주세요.")
        String language,

        @Schema(description = "자기소개 (최대 500자)", example = "언어의 경계를 넘어 더 나은 팀을 만듭니다.")
        @Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
        String bio
) {}
