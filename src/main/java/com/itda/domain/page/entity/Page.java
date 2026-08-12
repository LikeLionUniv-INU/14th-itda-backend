package com.itda.domain.page.entity;

import com.itda.domain.document.entity.DocumentVersion;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "screen_name", length = 10)
    private String screenName;

    @Column(name = "screen_id", length = 10)
    private String screenId;

    @Builder
    public Page(DocumentVersion documentVersion, Integer pageNumber,
                String screenName, String screenId) {
        this.documentVersion = documentVersion;
        this.pageNumber = pageNumber;
        this.screenName = screenName;
        this.screenId = screenId;
    }

    public void updatePageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void update(String screenName, String screenId) {
        this.screenName = screenName;
        this.screenId = screenId;
    }
}
