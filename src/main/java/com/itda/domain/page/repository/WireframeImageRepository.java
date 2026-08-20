package com.itda.domain.page.repository;

import com.itda.domain.page.entity.WireframeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WireframeImageRepository extends JpaRepository<WireframeImage, Long> {

    List<WireframeImage> findByPage_Id(Long pageId);

    void deleteByPage_Id(Long pageId);
}
