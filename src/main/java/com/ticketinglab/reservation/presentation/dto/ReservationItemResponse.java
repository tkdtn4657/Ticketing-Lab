package com.ticketinglab.reservation.presentation.dto;

import com.ticketinglab.reservation.domain.ReservationItem;

public record ReservationItemResponse(
        String type,
        Long seatId,
        int unitPrice
) {
    public static ReservationItemResponse from(ReservationItem item) {
        return new ReservationItemResponse(
                item.getType().name(),
                item.getSeatId(),
                item.getUnitPrice()
        );
    }
}
