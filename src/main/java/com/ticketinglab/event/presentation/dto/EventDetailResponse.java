package com.ticketinglab.event.presentation.dto;

import java.util.List;

public record EventDetailResponse(
        EventDetailInfoResponse event,
        List<ShowResponse> shows
) {
}
