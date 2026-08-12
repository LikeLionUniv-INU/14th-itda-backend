package com.itda.domain.page.dto.response;

public record PresignedUrlResponse(
        String presignedUrl,
        String fileUrl,
        String key
) {}
