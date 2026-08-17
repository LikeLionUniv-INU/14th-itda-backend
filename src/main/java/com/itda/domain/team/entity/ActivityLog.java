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
@Table(name = "activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_project_id", nullable = false)
    private TeamProject teamProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "document_name", nullable = false, length = 10)
    private String documentName;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ActivityLog(TeamProject teamProject, Document document,
                       String actionType, String documentName,
                       String documentType, Integer version, User performedBy) {
        this.teamProject = teamProject;
        this.document = document;
        this.actionType = actionType;
        this.documentName = documentName;
        this.documentType = documentType;
        this.version = version;
        this.performedBy = performedBy;
    }
}
