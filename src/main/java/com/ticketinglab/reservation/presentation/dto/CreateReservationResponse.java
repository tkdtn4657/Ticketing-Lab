package com.ticketinglab.reservation.presentation.dto;

public record CreateReservationResponse(
        String reservationId,
        String status
) {
}