package com.itda.domain.document.dto.request;

import java.util.List;

public record SaveDocumentRequest(
        String status,
        String changeSummary,
        List<PageData> pages
) {
    public record PageData(
            int pageNumber,
            String screenName,
            String screenId,
            List<PinData> pins
    ) {}

    public record PinData(
            int pinNumber,
            String tabType,
            double xCoordinate,
            double yCoordinate,
            List<RequirementData> requirements
    ) {}

    public record RequirementData(
            String tabType,
            String itemName,
            String content,
            Boolean isRequired
    ) {}
}
