package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.event.domain.Event;

import java.time.LocalDateTime;

public record AdminEventResponse(
        Long eventId,
        String title,
        String description,
        String status,
        Long createdByUserId,
        LocalDateTime createdAt
) {
    public static AdminEventResponse from(Event event) {
        return new AdminEventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus().name(),
                event.getCreatedByUserId(),
                event.getCreatedAt()
        );
    }
}
