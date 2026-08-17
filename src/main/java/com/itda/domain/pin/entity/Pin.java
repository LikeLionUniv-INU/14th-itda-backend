package com.itda.domain.pin.entity;

import com.itda.domain.page.entity.Page;
import com.itda.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "pin_number", nullable = false)
    private Integer pinNumber;

    @Column(name = "x_coordinate", nullable = false)
    private Double xCoordinate;

    @Column(name = "y_coordinate", nullable = false)
    private Double yCoordinate;

    @Builder
    public Pin(Page page, Integer pinNumber, Double xCoordinate, Double yCoordinate) {
        this.page = page;
        this.pinNumber = pinNumber;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public void updatePosition(Double xCoordinate, Double yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public void updatePinNumber(Integer pinNumber) {
        this.pinNumber = pinNumber;
    }
}
