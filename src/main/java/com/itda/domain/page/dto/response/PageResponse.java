package com.itda.domain.page.dto.response;

import com.itda.domain.page.entity.Page;

import java.time.LocalDateTime;

public record PageResponse(
        Long id,
        int pageNumber,
        String screenName,
        String screenId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PageResponse from(Page page) {
        return new PageResponse(
                page.getId(),
                page.getPageNumber(),
                page.getScreenName(),
                page.getScreenId(),
                page.getCreatedAt(),
                page.getUpdatedAt()
        );
    }
}
