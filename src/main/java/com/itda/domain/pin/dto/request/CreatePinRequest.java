package com.itda.domain.pin.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePinRequest(
        @NotNull(message = "X 좌표를 입력해주세요.")
        Double xCoordinate,

        @NotNull(message = "Y 좌표를 입력해주세요.")
        Double yCoordinate
) {}
