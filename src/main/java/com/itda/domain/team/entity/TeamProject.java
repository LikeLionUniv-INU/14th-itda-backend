package com.itda.domain.team.entity;

import com.itda.domain.user.entity.User;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamProject extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "default_language", nullable = false, length = 20)
    private String defaultLanguage;

    @Column(name = "invite_code", nullable = false, unique = true, length = 6)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder
    public TeamProject(String name, String defaultLanguage, String inviteCode, User createdBy) {
        this.name = name;
        this.defaultLanguage = defaultLanguage;
        this.inviteCode = inviteCode;
        this.createdBy = createdBy;
    }
}
