package com.itda.domain.translation.repository;

import com.itda.domain.translation.entity.TranslationJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationJobRepository extends JpaRepository<TranslationJob, Long> {
}
