package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.event.domain.Show;

import java.time.LocalDateTime;

public record AdminShowResponse(
        Long showId,
        Long eventId,
        String eventTitle,
        Long venueId,
        LocalDateTime startAt,
        String status,
        Long createdByUserId,
        LocalDateTime createdAt
) {
    public static AdminShowResponse from(Show show) {
        return new AdminShowResponse(
                show.getId(),
                show.getEvent().getId(),
                show.getEvent().getTitle(),
                show.getVenueId(),
                show.getStartAt(),
                show.getStatus().name(),
                show.getCreatedByUserId(),
                show.getCreatedAt()
        );
    }
}
