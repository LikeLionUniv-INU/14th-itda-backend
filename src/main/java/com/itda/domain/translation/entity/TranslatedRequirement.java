package com.itda.domain.translation.entity;

import com.itda.domain.requirement.entity.Requirement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "translated_requirements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslatedRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translation_language_id", nullable = false)
    private TranslationLanguage translationLanguage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private Requirement requirement;

    @Column(name = "translated_item_name", length = 50)
    private String translatedItemName;

    @Column(name = "translated_content", length = 500)
    private String translatedContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public TranslatedRequirement(TranslationLanguage translationLanguage,
                                  Requirement requirement,
                                  String translatedItemName,
                                  String translatedContent) {
        this.translationLanguage = translationLanguage;
        this.requirement = requirement;
        this.translatedItemName = translatedItemName;
        this.translatedContent = translatedContent;
    }
}
