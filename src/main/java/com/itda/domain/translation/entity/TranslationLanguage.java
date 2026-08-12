package com.itda.domain.translation.entity;

import com.itda.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "translation_languages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translation_job_id", nullable = false)
    private TranslationJob translationJob;

    @Column(name = "target_language", nullable = false, length = 20)
    private String targetLanguage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public TranslationLanguage(TranslationJob translationJob, String targetLanguage,
                                User targetUser) {
        this.translationJob = translationJob;
        this.targetLanguage = targetLanguage;
        this.targetUser = targetUser;
        this.status = "PENDING";
    }

    public void markTranslating() {
        this.status = "TRANSLATING";
    }

    public void markCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
