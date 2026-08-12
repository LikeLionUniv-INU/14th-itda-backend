package com.itda.domain.team.repository;

import com.itda.domain.team.entity.TeamProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamProjectRepository extends JpaRepository<TeamProject, Long> {

    Optional<TeamProject> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
