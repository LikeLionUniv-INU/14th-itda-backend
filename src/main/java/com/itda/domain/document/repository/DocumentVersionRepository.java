package com.itda.domain.document.repository;

import com.itda.domain.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocument_IdOrderByVersionDesc(Long documentId);

    Optional<DocumentVersion> findByDocument_IdAndVersion(Long documentId, Integer version);

    Optional<DocumentVersion> findTopByDocument_IdOrderByVersionDesc(Long documentId);

    int countByDocument_Id(Long documentId);
}
