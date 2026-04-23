package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.venue.domain.Venue;

import java.util.List;

public record AdminVenueListResponse(List<AdminVenueResponse> venues) {

    public static AdminVenueListResponse from(List<Venue> venues) {
        return new AdminVenueListResponse(
                venues.stream()
                        .map(AdminVenueResponse::from)
                        .toList()
        );
    }
}
