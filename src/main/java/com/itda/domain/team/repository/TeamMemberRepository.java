package com.itda.domain.team.repository;

import com.itda.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByUser_Id(Long userId);

    List<TeamMember> findByTeamProject_Id(Long teamProjectId);

    Optional<TeamMember> findByTeamProject_IdAndUser_Id(Long teamProjectId, Long userId);

    boolean existsByTeamProject_IdAndUser_Id(Long teamProjectId, Long userId);
}
