package com.itda.domain.document.entity;

import com.itda.domain.user.entity.User;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_versions",
        uniqueConstraints = @UniqueConstraint(name = "uq_document_versions",
                columnNames = {"document_id", "version"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentVersion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "is_auto_saved", nullable = false)
    private Boolean isAutoSaved;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder
    public DocumentVersion(Document document, Integer version, String status,
                           Boolean isAutoSaved, String changeSummary, User createdBy) {
        this.document = document;
        this.version = version;
        this.status = status;
        this.isAutoSaved = isAutoSaved != null ? isAutoSaved : false;
        this.changeSummary = changeSummary;
        this.createdBy = createdBy;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateForSave(String status, boolean isAutoSaved, String changeSummary) {
        this.status = status;
        this.isAutoSaved = isAutoSaved;
        this.changeSummary = changeSummary;
    }
}
