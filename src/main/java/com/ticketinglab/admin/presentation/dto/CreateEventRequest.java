package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank String title,
        String desc,
        @NotBlank String status
) {
}