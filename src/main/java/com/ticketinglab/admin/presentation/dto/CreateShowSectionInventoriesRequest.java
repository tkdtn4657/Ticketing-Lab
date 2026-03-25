package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateShowSectionInventoriesRequest(
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotNull Long sectionId,
            @Positive int price,
            @Positive int capacity
    ) {
    }
}