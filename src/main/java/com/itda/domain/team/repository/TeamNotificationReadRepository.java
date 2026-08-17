package com.itda.domain.team.repository;

import com.itda.domain.team.entity.TeamNotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamNotificationReadRepository extends JpaRepository<TeamNotificationRead, Long> {

    boolean existsByNotification_IdAndUser_Id(Long notificationId, Long userId);
}
