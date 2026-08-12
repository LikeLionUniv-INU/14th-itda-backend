package com.itda.domain.dashboard.dto.response;

import java.util.List;

public record DocumentListResponse(
        List<DashboardDocumentResponse> documents
) {}
