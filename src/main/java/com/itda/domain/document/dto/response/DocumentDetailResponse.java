package com.itda.domain.document.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentDetailResponse(
        Long documentId,
        String name,
        String language,
        String documentType,
        int version,
        String status,
        boolean isAutoSaved,
        String changeSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PageInfo> pages
) {
    public record PageInfo(
            Long id,
            int pageNumber,
            String screenName,
            String screenId,
            List<WireframeImageInfo> wireframeImages,
            List<PinInfo> pins
    ) {}

    public record WireframeImageInfo(
            Long id,
            String imageType,
            String imageUrl,
            Integer originalWidth,
            Integer originalHeight,
            Integer displayWidth,
            Integer displayHeight
    ) {}

    public record PinInfo(
            Long id,
            int pinNumber,
            double xCoordinate,
            double yCoordinate,
            List<RequirementInfo> requirements
    ) {}

    public record RequirementInfo(
            Long id,
            String tabType,
            String itemName,
            String content,
            boolean isRequired
    ) {}
}
