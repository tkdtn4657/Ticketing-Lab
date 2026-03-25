package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RegisterVenueSeatsRequest(
        @NotEmpty List<@Valid SeatItem> seats
) {
    public record SeatItem(
            @NotBlank String label,
            @Positive Integer rowNo,
            @Positive Integer colNo
    ) {
    }
}