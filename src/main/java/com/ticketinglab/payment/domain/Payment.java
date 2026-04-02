package com.ticketinglab.payment.domain;

import com.ticketinglab.reservation.domain.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_idempotency_key", columnNames = "idempotency_key")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Payment(
            Reservation reservation,
            PaymentProvider provider,
            String idempotencyKey,
            PaymentStatus status,
            int amount,
            LocalDateTime approvedAt,
            String rawPayload,
            LocalDateTime createdAt
    ) {
        this.reservation = reservation;
        this.provider = provider;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.amount = amount;
        this.approvedAt = approvedAt;
        this.rawPayload = rawPayload;
        this.createdAt = createdAt;
    }

    public static Payment approve(
            Reservation reservation,
            String idempotencyKey,
            int amount,
            String rawPayload
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(
                reservation,
                PaymentProvider.SIMULATOR,
                idempotencyKey,
                PaymentStatus.APPROVED,
                amount,
                now,
                rawPayload,
                now
        );
    }

    public boolean matches(Long userId, String reservationId, int amount) {
        return reservation.getId().equals(reservationId)
                && reservation.isOwnedBy(userId)
                && this.amount == amount;
    }
}