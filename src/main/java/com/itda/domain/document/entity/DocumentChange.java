package com.itda.domain.document.entity;

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
@Table(name = "document_changes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DocumentChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @Column(name = "change_type", nullable = false, length = 30)
    private String changeType;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "screen_name", length = 10)
    private String screenName;

    @Column(name = "pin_number")
    private Integer pinNumber;

    @Column(name = "item_description", length = 100)
    private String itemDescription;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DocumentChange(DocumentVersion documentVersion, String changeType,
                          Integer pageNumber, String screenName, Integer pinNumber,
                          String itemDescription, String beforeValue, String afterValue,
                          User modifiedBy) {
        this.documentVersion = documentVersion;
        this.changeType = changeType;
        this.pageNumber = pageNumber;
        this.screenName = screenName;
        this.pinNumber = pinNumber;
        this.itemDescription = itemDescription;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.modifiedBy = modifiedBy;
    }
}
