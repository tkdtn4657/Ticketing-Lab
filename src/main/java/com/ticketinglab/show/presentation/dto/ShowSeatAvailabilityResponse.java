package com.ticketinglab.show.presentation.dto;

import com.ticketinglab.show.domain.ShowSeat;

public record ShowSeatAvailabilityResponse(
        Long seatId,
        String label,
        Integer rowNo,
        Integer colNo,
        Long sectionId,
        String sectionName,
        String sectionSaleType,
        int price,
        boolean available
) {
    public static ShowSeatAvailabilityResponse from(ShowSeat showSeat) {
        return new ShowSeatAvailabilityResponse(
                showSeat.getSeat().getId(),
                showSeat.getSeat().getLabel(),
                showSeat.getSeat().getRowNo(),
                showSeat.getSeat().getColNo(),
                showSeat.getSeat().getSection() == null ? null : showSeat.getSeat().getSection().getId(),
                showSeat.getSeat().getSection() == null ? null : showSeat.getSeat().getSection().getName(),
                showSeat.getSeat().getSection() == null ? null : showSeat.getSeat().getSection().getSaleType().name(),
                showSeat.getPrice(),
                showSeat.isAvailable()
        );
    }
}
