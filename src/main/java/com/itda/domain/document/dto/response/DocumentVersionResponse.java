package com.itda.domain.document.dto.response;

import com.itda.domain.document.entity.DocumentVersion;

import java.time.LocalDateTime;

public record DocumentVersionResponse(
        Long id,
        int version,
        String status,
        boolean isAutoSaved,
        String changeSummary,
        String createdByFirstName,
        String createdByLastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DocumentVersionResponse from(DocumentVersion dv) {
        return new DocumentVersionResponse(
                dv.getId(),
                dv.getVersion(),
                dv.getStatus(),
                dv.getIsAutoSaved(),
                dv.getChangeSummary(),
                dv.getCreatedBy().getFirstName(),
                dv.getCreatedBy().getLastName(),
                dv.getCreatedAt(),
                dv.getUpdatedAt()
        );
    }
}
