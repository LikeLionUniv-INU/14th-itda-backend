package com.itda.domain.team.entity;

import com.itda.domain.document.entity.Document;
import com.itda.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TeamNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_project_id", nullable = false)
    private TeamProject teamProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "document_name", nullable = false, length = 100)
    private String documentName;

    @Column(name = "before_version")
    private Integer beforeVersion;

    @Column(name = "after_version", nullable = false)
    private Integer afterVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TeamNotification(TeamProject teamProject, Document document,
                            String documentName, Integer beforeVersion,
                            Integer afterVersion, User performedBy) {
        this.teamProject = teamProject;
        this.document = document;
        this.documentName = documentName;
        this.beforeVersion = beforeVersion;
        this.afterVersion = afterVersion;
        this.performedBy = performedBy;
    }
}
