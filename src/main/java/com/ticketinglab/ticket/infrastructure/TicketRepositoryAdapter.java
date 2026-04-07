package com.ticketinglab.ticket.infrastructure;

import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
import com.ticketinglab.ticket.infrastructure.jpa.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {

    private final TicketJpaRepository jpaRepository;

    @Override
    public List<Ticket> saveAll(List<Ticket> tickets) {
        return jpaRepository.saveAll(tickets);
    }

    @Override
    public Page<Ticket> findPageByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findPageByUserId(userId, pageable);
    }

    @Override
    public List<Ticket> findAllByReservationId(String reservationId) {
        return jpaRepository.findAllByReservationId(reservationId);
    }

    @Override
    public Optional<Ticket> findByQrTokenForUpdate(String qrToken) {
        return jpaRepository.findByQrTokenForUpdate(qrToken);
    }
}
