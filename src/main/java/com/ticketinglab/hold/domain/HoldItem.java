package com.ticketinglab.hold.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hold_items")
public class HoldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private HoldItemType type;

    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hold_id", nullable = false)
    private Hold hold;

    private HoldItem(
            Hold hold,
            HoldItemType type,
            Long seatId,
            Long sectionId,
            int qty,
            int unitPrice
    ) {
        this.hold = hold;
        this.type = type;
        this.seatId = seatId;
        this.sectionId = sectionId;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public static HoldItem seat(Hold hold, Long seatId, int unitPrice) {
        return new HoldItem(hold, HoldItemType.SEAT, seatId, null, 1, unitPrice);
    }

    public static HoldItem section(Hold hold, Long sectionId, int qty, int unitPrice) {
        return new HoldItem(hold, HoldItemType.SECTION, null, sectionId, qty, unitPrice);
    }
}
