package com.ticketinglab.reservation.presentation.dto;

import com.ticketinglab.reservation.domain.Reservation;

import java.time.LocalDateTime;

public record ReservationSummaryResponse(
        String reservationId,
        Long showId,
        String status,
        int totalAmount,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static ReservationSummaryResponse from(Reservation reservation) {
        return new ReservationSummaryResponse(
                reservation.getId(),
                reservation.getShowId(),
                reservation.getStatus().name(),
                reservation.getTotalAmount(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt()
        );
    }
}