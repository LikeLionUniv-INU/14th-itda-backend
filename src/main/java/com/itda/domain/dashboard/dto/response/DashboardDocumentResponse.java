package com.itda.domain.dashboard.dto.response;

import java.time.LocalDateTime;

public record DashboardDocumentResponse(
        Long id,
        String name,
        Long teamProjectId,
        String teamProjectName,
        String language,
        String documentType,
        int latestVersion,
        LocalDateTime updatedAt
) {}
