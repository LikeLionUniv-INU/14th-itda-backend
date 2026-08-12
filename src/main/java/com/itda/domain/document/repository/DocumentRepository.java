package com.itda.domain.document.repository;

import com.itda.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTeamProject_Id(Long teamProjectId);

    List<Document> findByTeamProject_IdIn(List<Long> teamProjectIds);
}
