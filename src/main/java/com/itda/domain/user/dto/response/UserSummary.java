package com.itda.domain.user.dto.response;

import com.itda.domain.user.entity.User;

public record UserSummary(
        Long id,
        String firstName,
        String lastName,
        String initial
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getLastName().substring(0, 1).toUpperCase()
        );
    }
}
