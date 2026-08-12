package com.itda.domain.team.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TeamDetailResponse(
        Long id,
        String name,
        String defaultLanguage,
        String inviteCode,
        LocalDateTime createdAt,
        String myRole,
        List<TeamMemberInfo> members,
        List<TeamDocumentInfo> documents,
        List<String> memberLanguages
) {
    public record TeamMemberInfo(
            Long id,
            String firstName,
            String lastName,
            String initial,
            String role,
            String language,
            String country
    ) {}

    public record TeamDocumentInfo(
            Long id,
            String name,
            String language,
            int latestVersion,
            List<Integer> versions,
            LocalDateTime updatedAt,
            UpdatedByInfo updatedBy
    ) {}

    public record UpdatedByInfo(
            String firstName,
            String lastName
    ) {}
}
