package com.ticketinglab.reservation.presentation.dto;

import com.ticketinglab.reservation.domain.Reservation;

import java.util.List;

public record ReservationDetailResponse(
        ReservationSummaryResponse reservation,
        List<ReservationItemResponse> items
) {
    public static ReservationDetailResponse from(Reservation reservation) {
        return new ReservationDetailResponse(
                ReservationSummaryResponse.from(reservation),
                reservation.getItems().stream()
                        .map(ReservationItemResponse::from)
                        .toList()
        );
    }
}