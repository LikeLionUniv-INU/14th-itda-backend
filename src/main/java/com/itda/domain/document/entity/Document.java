package com.itda.domain.document.entity;

import com.itda.domain.team.entity.TeamProject;
import com.itda.domain.user.entity.User;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_project_id", nullable = false)
    private TeamProject teamProject;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder
    public Document(TeamProject teamProject, String name, String language,
                    String documentType, User createdBy) {
        this.teamProject = teamProject;
        this.name = name;
        this.language = language;
        this.documentType = documentType;
        this.createdBy = createdBy;
    }
}
