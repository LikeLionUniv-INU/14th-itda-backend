package com.itda.domain.document.repository;

import com.itda.domain.document.entity.ChangeConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeConfirmationRepository extends JpaRepository<ChangeConfirmation, Long> {

    boolean existsByDocumentChange_IdAndConfirmedBy_Id(Long documentChangeId, Long userId);

    List<ChangeConfirmation> findByDocumentChange_DocumentVersion_IdAndConfirmedBy_Id(
            Long documentVersionId, Long userId);
}
