package com.ticketinglab.event.presentation.dto;

import com.ticketinglab.event.domain.Event;

import java.time.LocalDateTime;

public record EventSummaryResponse(
        Long eventId,
        String title,
        String description,
        String status,
        LocalDateTime createdAt
) {
    public static EventSummaryResponse from(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus().name(),
                event.getCreatedAt()
        );
    }
}
