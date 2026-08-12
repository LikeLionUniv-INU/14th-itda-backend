package com.itda.domain.auth.dto.response;

import com.itda.domain.user.entity.User;

import java.time.LocalDateTime;

public record SignupResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String country,
        String language,
        LocalDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getLanguage(),
                user.getCreatedAt()
        );
    }
}
