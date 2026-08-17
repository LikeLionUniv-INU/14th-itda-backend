package com.itda.domain.requirement.dto.response;

import com.itda.domain.requirement.entity.Requirement;

import java.time.LocalDateTime;

public record RequirementResponse(
        Long id,
        String tabType,
        String itemName,
        String content,
        boolean isRequired,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RequirementResponse from(Requirement requirement) {
        return new RequirementResponse(
                requirement.getId(),
                requirement.getTabType(),
                requirement.getItemName(),
                requirement.getContent(),
                requirement.getIsRequired(),
                requirement.getCreatedAt(),
                requirement.getUpdatedAt()
        );
    }
}
