package com.itda.domain.requirement.entity;

import com.itda.domain.pin.entity.Pin;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "requirements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Requirement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @Column(name = "original_id")
    private Long originalId;

    @Column(name = "tab_type", nullable = false, length = 20)
    private String tabType;

    @Column(name = "item_name", length = 10)
    private String itemName;

    @Column(length = 200)
    private String content;

    @Builder
    public Requirement(Pin pin, Long originalId, String tabType,
                       String itemName, String content) {
        this.pin = pin;
        this.originalId = originalId;
        this.tabType = tabType;
        this.itemName = itemName;
        this.content = content;
    }

    public void update(String itemName, String content) {
        this.itemName = itemName;
        this.content = content;
    }
}
