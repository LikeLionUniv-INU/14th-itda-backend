package com.itda.domain.translation.dto.response;

import com.itda.domain.translation.entity.TranslationJob;
import com.itda.domain.translation.entity.TranslationLanguage;

import java.time.LocalDateTime;
import java.util.List;

public record TranslationJobResponse(
        Long jobId,
        String status,
        int totalLanguages,
        int completedLanguages,
        int progress,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        List<LanguageStatus> languages
) {
    public record LanguageStatus(
            Long id,
            String targetLanguage,
            String status,
            LocalDateTime completedAt
    ) {
        public static LanguageStatus from(TranslationLanguage tl) {
            return new LanguageStatus(
                    tl.getId(),
                    tl.getTargetLanguage(),
                    tl.getStatus(),
                    tl.getCompletedAt()
            );
        }
    }

    public static TranslationJobResponse from(TranslationJob job, List<TranslationLanguage> languages) {
        int progress = job.getTotalLanguages() > 0
                ? (int) ((job.getCompletedLanguages() * 100.0) / job.getTotalLanguages())
                : 0;

        return new TranslationJobResponse(
                job.getId(),
                job.getStatus(),
                job.getTotalLanguages(),
                job.getCompletedLanguages(),
                progress,
                job.getCreatedAt(),
                job.getCompletedAt(),
                languages.stream().map(LanguageStatus::from).toList()
        );
    }
}
