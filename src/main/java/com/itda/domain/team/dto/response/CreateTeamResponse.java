package com.itda.domain.team.dto.response;

import com.itda.domain.team.entity.TeamProject;
import com.itda.domain.user.entity.User;

import java.time.LocalDateTime;

public record CreateTeamResponse(
        Long id,
        String name,
        String defaultLanguage,
        String inviteCode,
        CreatorInfo createdBy,
        LocalDateTime createdAt
) {
    public record CreatorInfo(Long id, String firstName, String lastName) {}

    public static CreateTeamResponse of(TeamProject team, User creator) {
        return new CreateTeamResponse(
                team.getId(),
                team.getName(),
                team.getDefaultLanguage(),
                team.getInviteCode(),
                new CreatorInfo(creator.getId(), creator.getFirstName(), creator.getLastName()),
                team.getCreatedAt()
        );
    }
}
