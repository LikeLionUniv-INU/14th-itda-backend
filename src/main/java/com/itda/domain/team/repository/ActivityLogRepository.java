package com.itda.domain.team.repository;

import com.itda.domain.team.entity.ActivityLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @EntityGraph(attributePaths = {"performedBy"})
    List<ActivityLog> findTop10ByTeamProject_IdOrderByCreatedAtDesc(Long teamProjectId);
}
