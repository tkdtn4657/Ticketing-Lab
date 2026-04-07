package com.ticketinglab.ticket.domain;

import com.ticketinglab.reservation.domain.ReservationItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_item_id", nullable = false)
    private ReservationItem reservationItem;

    @Column(name = "serial", nullable = false, length = 40, unique = true)
    private String serial;

    @Column(name = "qr_token", nullable = false, length = 80, unique = true)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Ticket(
            String id,
            ReservationItem reservationItem,
            String serial,
            String qrToken,
            TicketStatus status,
            LocalDateTime usedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.reservationItem = reservationItem;
        this.serial = serial;
        this.qrToken = qrToken;
        this.status = status;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public static Ticket issue(ReservationItem reservationItem) {
        return new Ticket(
                UUID.randomUUID().toString(),
                reservationItem,
                "TKT-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(),
                UUID.randomUUID().toString(),
                TicketStatus.ISSUED,
                null,
                LocalDateTime.now()
        );
    }

    public void checkIn(LocalDateTime checkedInAt) {
        if (status == TicketStatus.USED) {
            throw new IllegalStateException("ticket already used");
        }
        if (status != TicketStatus.ISSUED) {
            throw new IllegalStateException("ticket not available for checkin");
        }

        this.status = TicketStatus.USED;
        this.usedAt = Objects.requireNonNull(checkedInAt, "checkedInAt");
    }
}
