package com.itda.domain.dashboard.dto.response;

import com.itda.domain.user.dto.response.UserSummary;

import java.util.List;

public record DashboardResponse(
        UserSummary user,
        List<DashboardProjectResponse> projects,
        List<DashboardDocumentResponse> recentDocuments
) {}
