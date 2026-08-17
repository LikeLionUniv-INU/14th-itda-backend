package com.itda.domain.pin.dto.response;

import java.util.List;

public record PinDetailResponse(
        Long id,
        int pinNumber,
        String tabType,
        double xCoordinate,
        double yCoordinate,
        List<RequirementInfo> requirements
) {
    public record RequirementInfo(
            Long id,
            String tabType,
            String itemName,
            String content,
            boolean isRequired
    ) {}
}
