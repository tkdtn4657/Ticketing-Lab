package com.ticketinglab.event.presentation.dto;

import com.ticketinglab.event.domain.Event;

import java.time.LocalDateTime;

public record EventDetailInfoResponse(
        Long eventId,
        String title,
        String description,
        String status,
        LocalDateTime createdAt
) {
    public static EventDetailInfoResponse from(Event event) {
        return new EventDetailInfoResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus().name(),
                event.getCreatedAt()
        );
    }
}
