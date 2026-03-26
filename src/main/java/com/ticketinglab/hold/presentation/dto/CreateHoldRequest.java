package com.ticketinglab.hold.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateHoldRequest(
        @NotNull @Positive Long showId,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @Positive Long seatId,
            @Positive Long sectionId,
            @Positive Integer qty
    ) {
    }
}
