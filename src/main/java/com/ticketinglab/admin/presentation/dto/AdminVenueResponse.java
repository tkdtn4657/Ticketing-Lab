package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.venue.domain.Venue;

import java.time.LocalDateTime;

public record AdminVenueResponse(
        Long venueId,
        String code,
        String name,
        String address,
        Long createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminVenueResponse from(Venue venue) {
        return new AdminVenueResponse(
                venue.getId(),
                venue.getCode(),
                venue.getName(),
                venue.getAddress(),
                venue.getCreatedByUserId(),
                venue.getCreatedAt(),
                venue.getUpdatedAt()
        );
    }
}
