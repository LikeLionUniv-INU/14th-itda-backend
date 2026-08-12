package com.itda.domain.pin.repository;

import com.itda.domain.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findByPage_IdOrderByPinNumberAsc(Long pageId);

    int countByPage_Id(Long pageId);

    void deleteByPage_Id(Long pageId);
}
