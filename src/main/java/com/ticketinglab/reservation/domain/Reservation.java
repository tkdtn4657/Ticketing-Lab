package com.ticketinglab.reservation.domain;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationItem> items = new ArrayList<>();

    private Reservation(
            String id,
            ReservationStatus status,
            int totalAmount,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            Long userId,
            Long showId
    ) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.userId = userId;
        this.showId = showId;
    }

    public static Reservation createFromHold(Hold hold, LocalDateTime expiresAt) {
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                ReservationStatus.PENDING_PAYMENT,
                0,
                expiresAt,
                LocalDateTime.now(),
                hold.getUserId(),
                hold.getShowId()
        );

        for (HoldItem holdItem : hold.getItems()) {
            reservation.addItem(holdItem);
        }

        return reservation;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isPendingPayment() {
        return status == ReservationStatus.PENDING_PAYMENT;
    }

    public boolean isExpiredAt(LocalDateTime at) {
        return expiresAt != null && !expiresAt.isAfter(at);
    }

    public void expire(LocalDateTime at) {
        if (!isPendingPayment()) {
            return;
        }
        if (!isExpiredAt(at)) {
            throw new IllegalStateException("reservation not expired");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    private void addItem(HoldItem holdItem) {
        if (holdItem.getSeatId() != null) {
            items.add(ReservationItem.seat(this, holdItem.getSeatId(), holdItem.getUnitPrice()));
            totalAmount += holdItem.getUnitPrice();
            return;
        }

        items.add(ReservationItem.section(this, holdItem.getSectionId(), holdItem.getQty(), holdItem.getUnitPrice()));
        totalAmount += holdItem.getQty() * holdItem.getUnitPrice();
    }
}