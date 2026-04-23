package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.event.domain.Event;

import java.util.List;

public record AdminEventListResponse(List<AdminEventResponse> events) {

    public static AdminEventListResponse from(List<Event> events) {
        return new AdminEventListResponse(
                events.stream()
                        .map(AdminEventResponse::from)
                        .toList()
        );
    }
}
