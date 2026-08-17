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
@Table(name = "change_confirmations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"document_change_id", "confirmed_by"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChangeConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_change_id", nullable = false)
    private DocumentChange documentChange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @CreatedDate
    @Column(name = "confirmed_at", nullable = false, updatable = false)
    private LocalDateTime confirmedAt;

    @Builder
    public ChangeConfirmation(DocumentChange documentChange, User confirmedBy) {
        this.documentChange = documentChange;
        this.confirmedBy = confirmedBy;
    }
}
