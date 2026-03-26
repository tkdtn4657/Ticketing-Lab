package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateShowRequest(
        @NotNull Long eventId,
        @NotNull Long venueId,
        @NotNull LocalDateTime startAt
) {
}