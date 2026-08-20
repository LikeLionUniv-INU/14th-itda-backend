package com.itda.domain.translation.repository;

import com.itda.domain.translation.entity.TranslationLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TranslationLanguageRepository extends JpaRepository<TranslationLanguage, Long> {

    List<TranslationLanguage> findByTranslationJob_Id(Long translationJobId);

    Optional<TranslationLanguage> findTopByTranslationJob_DocumentVersion_IdAndTargetLanguageAndStatusOrderByIdDesc(
            Long documentVersionId, String targetLanguage, String status);
}
