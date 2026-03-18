package com.ticketinglab.event.presentation.dto;

import java.util.List;

public record EventListResponse(
        List<EventSummaryResponse> events
) {
}
