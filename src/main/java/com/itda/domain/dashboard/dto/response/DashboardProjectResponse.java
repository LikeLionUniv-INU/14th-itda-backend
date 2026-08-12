package com.itda.domain.dashboard.dto.response;

import com.itda.domain.user.dto.response.MemberSummary;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardProjectResponse(
        Long id,
        String name,
        String defaultLanguage,
        List<String> memberLanguages,
        List<MemberSummary> members,
        int memberCount,
        int documentCount,
        LocalDateTime lastDocumentUpdatedAt
) {}
