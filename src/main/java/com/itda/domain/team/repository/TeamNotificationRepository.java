package com.itda.domain.team.repository;

import com.itda.domain.team.entity.TeamNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamNotificationRepository extends JpaRepository<TeamNotification, Long> {

    @Query("SELECT n FROM TeamNotification n " +
            "WHERE n.teamProject.id = :teamId " +
            "AND n.id NOT IN (" +
            "   SELECT r.notification.id FROM TeamNotificationRead r WHERE r.user.id = :userId" +
            ") " +
            "AND n.performedBy.id != :userId " +
            "ORDER BY n.createdAt DESC")
    List<TeamNotification> findUnreadByTeamAndUser(@Param("teamId") Long teamId,
                                                    @Param("userId") Long userId);
}
