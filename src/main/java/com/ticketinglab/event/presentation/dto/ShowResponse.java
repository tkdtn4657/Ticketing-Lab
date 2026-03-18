package com.ticketinglab.event.presentation.dto;

import com.ticketinglab.event.domain.Show;

import java.time.LocalDateTime;

public record ShowResponse(
        Long showId,
        LocalDateTime startAt,
        String status,
        Long venueId
) {
    public static ShowResponse from(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getStartAt(),
                show.getStatus().name(),
                show.getVenueId()
        );
    }
}
