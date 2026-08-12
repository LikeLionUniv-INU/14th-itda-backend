package com.itda.domain.translation.entity;

import com.itda.domain.document.entity.DocumentVersion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "translation_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_languages", nullable = false)
    private Integer totalLanguages;

    @Column(name = "completed_languages", nullable = false)
    private Integer completedLanguages;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public TranslationJob(DocumentVersion documentVersion, String status,
                           Integer totalLanguages) {
        this.documentVersion = documentVersion;
        this.status = status != null ? status : "PENDING";
        this.totalLanguages = totalLanguages;
        this.completedLanguages = 0;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void incrementCompletedLanguages() {
        this.completedLanguages++;
    }

    public void markCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
