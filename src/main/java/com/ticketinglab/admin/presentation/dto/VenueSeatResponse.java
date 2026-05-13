package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.venue.domain.Seat;

public record VenueSeatResponse(
        Long seatId,
        String label,
        Integer rowNo,
        Integer colNo,
        Long sectionId,
        String sectionName,
        String sectionSaleType
) {
    public static VenueSeatResponse from(Seat seat) {
        return new VenueSeatResponse(
                seat.getId(),
                seat.getLabel(),
                seat.getRowNo(),
                seat.getColNo(),
                seat.getSection() == null ? null : seat.getSection().getId(),
                seat.getSection() == null ? null : seat.getSection().getName(),
                seat.getSection() == null ? null : seat.getSection().getSaleType().name()
        );
    }
}
