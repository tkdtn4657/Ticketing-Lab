package com.ticketinglab.reservation.presentation.dto;

import java.util.List;

public record MyReservationListResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<ReservationSummaryResponse> reservations
) {
}