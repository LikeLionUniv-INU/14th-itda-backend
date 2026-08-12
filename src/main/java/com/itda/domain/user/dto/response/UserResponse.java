package com.itda.domain.user.dto.response;

import com.itda.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String country,
        String language,
        String initial,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getLanguage(),
                user.getLastName().substring(0, 1).toUpperCase(),
                user.getCreatedAt()
        );
    }
}
