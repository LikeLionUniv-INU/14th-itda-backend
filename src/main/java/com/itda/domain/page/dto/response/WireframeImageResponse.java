package com.itda.domain.page.dto.response;

import com.itda.domain.page.entity.WireframeImage;

import java.time.LocalDateTime;

public record WireframeImageResponse(
        Long id,
        String imageType,
        String imageUrl,
        Integer originalWidth,
        Integer originalHeight,
        Integer displayWidth,
        Integer displayHeight,
        LocalDateTime createdAt
) {
    public static WireframeImageResponse from(WireframeImage image) {
        return new WireframeImageResponse(
                image.getId(),
                image.getImageType(),
                image.getImageUrl(),
                image.getOriginalWidth(),
                image.getOriginalHeight(),
                image.getDisplayWidth(),
                image.getDisplayHeight(),
                image.getCreatedAt()
        );
    }
}
