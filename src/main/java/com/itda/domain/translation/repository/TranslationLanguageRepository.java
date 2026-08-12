package com.itda.domain.translation.repository;

import com.itda.domain.translation.entity.TranslationLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslationLanguageRepository extends JpaRepository<TranslationLanguage, Long> {

    List<TranslationLanguage> findByTranslationJob_Id(Long translationJobId);

    List<TranslationLanguage> findByTranslationJob_DocumentVersion_IdAndTargetLanguageAndStatus(
            Long documentVersionId, String targetLanguage, String status);
}
