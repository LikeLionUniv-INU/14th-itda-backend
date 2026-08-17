package com.itda.domain.pin.dto.response;

import com.itda.domain.pin.entity.Pin;

import java.time.LocalDateTime;

public record PinResponse(
        Long id,
        int pinNumber,
        String tabType,
        double xCoordinate,
        double yCoordinate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PinResponse from(Pin pin) {
        return new PinResponse(
                pin.getId(),
                pin.getPinNumber(),
                pin.getTabType(),
                pin.getXCoordinate(),
                pin.getYCoordinate(),
                pin.getCreatedAt(),
                pin.getUpdatedAt()
        );
    }
}
