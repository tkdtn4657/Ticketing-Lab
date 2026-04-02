package com.ticketinglab.ticket.application;

import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
import com.ticketinglab.ticket.presentation.dto.MyTicketListResponse;
import com.ticketinglab.ticket.presentation.dto.TicketSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMyTicketsUseCase {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public MyTicketListResponse execute(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Ticket> tickets = ticketRepository.findPageByUserId(userId, pageable);
        List<TicketSummaryResponse> responses = tickets.getContent().stream()
                .map(TicketSummaryResponse::from)
                .toList();

        return new MyTicketListResponse(
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                responses
        );
    }
}