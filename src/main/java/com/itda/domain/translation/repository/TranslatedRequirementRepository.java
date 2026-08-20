package com.itda.domain.translation.repository;

import com.itda.domain.translation.entity.TranslatedRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslatedRequirementRepository extends JpaRepository<TranslatedRequirement, Long> {

    List<TranslatedRequirement> findByTranslationLanguage_Id(Long translationLanguageId);

    List<TranslatedRequirement> findByRequirement_IdAndTranslationLanguage_Id(
            Long requirementId, Long translationLanguageId);

    List<TranslatedRequirement> findByRequirement_Id(Long requirementId);

    void deleteByRequirement_Id(Long requirementId);
}
