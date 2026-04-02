package com.ticketinglab.ticket.presentation.dto;

import java.util.List;

public record MyTicketListResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<TicketSummaryResponse> tickets
) {
}