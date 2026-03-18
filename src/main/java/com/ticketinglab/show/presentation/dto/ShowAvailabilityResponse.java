package com.ticketinglab.show.presentation.dto;

import java.util.List;

public record ShowAvailabilityResponse(
        List<ShowSeatAvailabilityResponse> seats,
        List<SectionAvailabilityResponse> sections
) {
}
