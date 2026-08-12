package com.itda.domain.page.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wireframe_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WireframeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "image_type", nullable = false, length = 10)
    private String imageType;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "original_width")
    private Integer originalWidth;

    @Column(name = "original_height")
    private Integer originalHeight;

    @Column(name = "display_width", nullable = false)
    private Integer displayWidth;

    @Column(name = "display_height", nullable = false)
    private Integer displayHeight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public WireframeImage(Page page, String imageType, String imageUrl,
                          Integer originalWidth, Integer originalHeight,
                          Integer displayWidth, Integer displayHeight) {
        this.page = page;
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    public void update(String imageType, String imageUrl,
                       Integer originalWidth, Integer originalHeight,
                       Integer displayWidth, Integer displayHeight) {
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }
}
