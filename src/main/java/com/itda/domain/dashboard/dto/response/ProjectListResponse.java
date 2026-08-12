package com.itda.domain.dashboard.dto.response;

import java.util.List;

public record ProjectListResponse(
        List<DashboardProjectResponse> projects
) {}
