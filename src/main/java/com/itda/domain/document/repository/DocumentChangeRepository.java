package com.itda.domain.document.repository;

import com.itda.domain.document.entity.DocumentChange;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChangeRepository extends JpaRepository<DocumentChange, Long> {

    @EntityGraph(attributePaths = {"modifiedBy"})
    List<DocumentChange> findByDocumentVersion_IdOrderByPageNumberAscPinNumberAsc(Long documentVersionId);

    void deleteByDocumentVersion_Id(Long documentVersionId);
}
