package com.ticketinglab.ticket.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    List<Ticket> saveAll(List<Ticket> tickets);
    Page<Ticket> findPageByUserId(Long userId, Pageable pageable);
    List<Ticket> findAllByReservationId(String reservationId);
    Optional<Ticket> findByQrTokenForUpdate(String qrToken);
}
