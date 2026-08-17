package com.itda.domain.pin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "핀 추가 요청")
public record CreatePinRequest(
        @Schema(description = "X 좌표", example = "150.5")
        @NotNull(message = "X 좌표를 입력해주세요.")
        Double xCoordinate,

        @Schema(description = "Y 좌표", example = "200.3")
        @NotNull(message = "Y 좌표를 입력해주세요.")
        Double yCoordinate,

        @Schema(description = "현재 활성 탭 유형 (미전달 시 '공통')", example = "공통")
        String tabType
) {}
