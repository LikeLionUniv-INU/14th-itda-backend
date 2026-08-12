package com.itda.domain.requirement.repository;

import com.itda.domain.requirement.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByPin_Id(Long pinId);

    void deleteByPin_Id(Long pinId);
}
