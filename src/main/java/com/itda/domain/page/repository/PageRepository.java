package com.itda.domain.page.repository;

import com.itda.domain.page.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Long> {

    List<Page> findByDocumentVersion_IdOrderByPageNumberAsc(Long documentVersionId);

    void deleteByDocumentVersion_Id(Long documentVersionId);
}
