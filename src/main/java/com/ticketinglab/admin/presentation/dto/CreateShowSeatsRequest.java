package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateShowSeatsRequest(
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotNull Long seatId,
            @Positive int price
    ) {
    }
}