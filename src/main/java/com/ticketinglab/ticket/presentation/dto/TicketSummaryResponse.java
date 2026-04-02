package com.ticketinglab.ticket.presentation.dto;

import com.ticketinglab.reservation.domain.ReservationItem;
import com.ticketinglab.ticket.domain.Ticket;

import java.time.LocalDateTime;

public record TicketSummaryResponse(
        String ticketId,
        String reservationId,
        Long showId,
        Long reservationItemId,
        String type,
        Long seatId,
        Long sectionId,
        String serial,
        String qrToken,
        String status,
        LocalDateTime usedAt,
        LocalDateTime createdAt
) {
    public static TicketSummaryResponse from(Ticket ticket) {
        ReservationItem reservationItem = ticket.getReservationItem();
        return new TicketSummaryResponse(
                ticket.getId(),
                reservationItem.getReservation().getId(),
                reservationItem.getReservation().getShowId(),
                reservationItem.getId(),
                reservationItem.getType().name(),
                reservationItem.getSeatId(),
                reservationItem.getSectionId(),
                ticket.getSerial(),
                ticket.getQrToken(),
                ticket.getStatus().name(),
                ticket.getUsedAt(),
                ticket.getCreatedAt()
        );
    }
}