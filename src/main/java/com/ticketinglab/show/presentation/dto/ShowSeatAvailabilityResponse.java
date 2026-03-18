package com.ticketinglab.show.presentation.dto;

import com.ticketinglab.show.domain.ShowSeat;

public record ShowSeatAvailabilityResponse(
        Long seatId,
        String label,
        Integer rowNo,
        Integer colNo,
        int price,
        boolean available
) {
    public static ShowSeatAvailabilityResponse from(ShowSeat showSeat) {
        return new ShowSeatAvailabilityResponse(
                showSeat.getSeat().getId(),
                showSeat.getSeat().getLabel(),
                showSeat.getSeat().getRowNo(),
                showSeat.getSeat().getColNo(),
                showSeat.getPrice(),
                showSeat.isAvailable()
        );
    }
}
