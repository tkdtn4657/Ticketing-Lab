package com.ticketinglab.reservation.domain;

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
@Table(name = "reservation_items")
public class ReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReservationItemType type;

    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private ReservationItem(
            Reservation reservation,
            ReservationItemType type,
            Long seatId,
            Long sectionId,
            int qty,
            int unitPrice
    ) {
        this.reservation = reservation;
        this.type = type;
        this.seatId = seatId;
        this.sectionId = sectionId;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public static ReservationItem seat(Reservation reservation, Long seatId, int unitPrice) {
        return new ReservationItem(reservation, ReservationItemType.SEAT, seatId, null, 1, unitPrice);
    }

    public static ReservationItem section(Reservation reservation, Long sectionId, int qty, int unitPrice) {
        return new ReservationItem(reservation, ReservationItemType.SECTION, null, sectionId, qty, unitPrice);
    }
}