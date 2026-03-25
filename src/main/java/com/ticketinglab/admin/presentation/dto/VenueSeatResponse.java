package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.venue.domain.Seat;

public record VenueSeatResponse(
        Long seatId,
        String label,
        Integer rowNo,
        Integer colNo
) {
    public static VenueSeatResponse from(Seat seat) {
        return new VenueSeatResponse(
                seat.getId(),
                seat.getLabel(),
                seat.getRowNo(),
                seat.getColNo()
        );
    }
}