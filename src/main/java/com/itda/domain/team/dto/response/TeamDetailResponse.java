package com.itda.domain.team.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
        List<String> memberLanguages,
        List<ActivityLogInfo> activityLogs
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
            String documentType,
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

    @Schema(description = "활동 로그")
    public record ActivityLogInfo(
            Long id,
            @Schema(description = "활동 유형 (UPLOADED / UPDATED)", example = "UPLOADED")
            String actionType,
            @Schema(description = "문서명", example = "스토리보드")
            String documentName,
            @Schema(description = "문서 유형", example = "STORYBOARD")
            String documentType,
            @Schema(description = "버전 번호", example = "3")
            Integer version,
            String performedByFirstName,
            String performedByLastName,
            @Schema(description = "수행자 이니셜", example = "H")
            String performedByInitial,
            LocalDateTime createdAt
    ) {}
}
