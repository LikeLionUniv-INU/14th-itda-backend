package com.itda.domain.document.dto.response;

import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;

import java.time.LocalDateTime;

public record CreateDocumentResponse(
        Long documentId,
        String name,
        String language,
        String documentType,
        int version,
        String status,
        LocalDateTime createdAt
) {
    public static CreateDocumentResponse of(Document document, DocumentVersion version) {
        return new CreateDocumentResponse(
                document.getId(),
                document.getName(),
                document.getLanguage(),
                document.getDocumentType(),
                version.getVersion(),
                version.getStatus(),
                document.getCreatedAt()
        );
    }
}
