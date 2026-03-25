package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record VenueUpsertRequest(
        @NotBlank String code,
        @NotBlank String name,
        String address
) {
}