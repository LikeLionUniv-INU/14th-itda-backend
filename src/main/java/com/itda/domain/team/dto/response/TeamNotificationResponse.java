package com.itda.domain.team.dto.response;

import com.itda.domain.team.entity.TeamNotification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 프로젝트 알림")
public record TeamNotificationResponse(
        Long id,
        @Schema(description = "문서명", example = "스토리보드")
        String documentName,
        @Schema(description = "변경 전 버전", example = "1")
        Integer beforeVersion,
        @Schema(description = "변경 후 버전", example = "2")
        Integer afterVersion,
        @Schema(description = "수행자 이름(성)")
        String performedByFirstName,
        @Schema(description = "수행자 이름(이름)")
        String performedByLastName,
        LocalDateTime createdAt
) {
    public static TeamNotificationResponse from(TeamNotification notification) {
        return new TeamNotificationResponse(
                notification.getId(),
                notification.getDocumentName(),
                notification.getBeforeVersion(),
                notification.getAfterVersion(),
                notification.getPerformedBy() != null ? notification.getPerformedBy().getFirstName() : null,
                notification.getPerformedBy() != null ? notification.getPerformedBy().getLastName() : null,
                notification.getCreatedAt()
        );
    }
}
