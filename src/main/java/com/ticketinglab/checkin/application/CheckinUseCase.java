package com.ticketinglab.checkin.application;

import com.ticketinglab.checkin.presentation.dto.CheckinResponse;
import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CheckinUseCase {

    private final TicketRepository ticketRepository;

    @Transactional
    public CheckinResponse execute(String qrToken) {
        Ticket ticket = ticketRepository.findByQrTokenForUpdate(qrToken)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ticket not found"));

        try {
            ticket.checkIn(LocalDateTime.now());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }

        return CheckinResponse.from(ticket);
    }
}
