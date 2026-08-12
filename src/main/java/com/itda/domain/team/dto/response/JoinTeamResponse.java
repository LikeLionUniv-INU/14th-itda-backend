package com.itda.domain.team.dto.response;

import com.itda.domain.team.entity.TeamMember;

import java.time.LocalDateTime;

public record JoinTeamResponse(
        Long teamProjectId,
        String name,
        String role,
        LocalDateTime joinedAt
) {
    public static JoinTeamResponse from(TeamMember member) {
        return new JoinTeamResponse(
                member.getTeamProject().getId(),
                member.getTeamProject().getName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
