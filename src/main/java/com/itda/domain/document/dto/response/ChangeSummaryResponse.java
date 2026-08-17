package com.itda.domain.document.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "수정사항 요약")
public record ChangeSummaryResponse(
        @Schema(description = "전체 수정사항 수") int totalChanges,
        @Schema(description = "내가 확인한 수") int confirmedByMe,
        @Schema(description = "미확인 수") int unconfirmedByMe,
        List<ChangeInfo> changes
) {
    @Schema(description = "개별 수정사항")
    public record ChangeInfo(
            Long id,
            @Schema(description = "변경 유형", example = "REQUIREMENT_MODIFIED")
            String changeType,
            int pageNumber,
            String screenName,
            Integer pinNumber,
            @Schema(description = "항목 설명", example = "ID입력")
            String itemDescription,
            @Schema(description = "변경 전 (JSON)")
            String beforeValue,
            @Schema(description = "변경 후 (JSON)")
            String afterValue,
            String modifiedByFirstName,
            String modifiedByLastName,
            LocalDateTime createdAt,
            @Schema(description = "내가 확인했는지 여부")
            boolean confirmedByMe
    ) {}
}
