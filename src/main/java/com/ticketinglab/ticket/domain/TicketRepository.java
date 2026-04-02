package com.ticketinglab.ticket.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketRepository {
    List<Ticket> saveAll(List<Ticket> tickets);
    Page<Ticket> findPageByUserId(Long userId, Pageable pageable);
    List<Ticket> findAllByReservationId(String reservationId);
}