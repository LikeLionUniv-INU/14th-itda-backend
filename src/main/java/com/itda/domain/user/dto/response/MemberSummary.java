package com.itda.domain.user.dto.response;

import com.itda.domain.user.entity.User;

public record MemberSummary(
        String firstName,
        String lastName,
        String initial
) {
    public static MemberSummary from(User user) {
        return new MemberSummary(
                user.getFirstName(),
                user.getLastName(),
                user.getLastName().substring(0, 1).toUpperCase()
        );
    }
}
