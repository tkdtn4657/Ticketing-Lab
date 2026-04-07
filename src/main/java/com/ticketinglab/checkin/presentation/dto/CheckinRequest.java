package com.ticketinglab.checkin.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckinRequest(
        @NotBlank
        @Size(max = 80)
        String qrToken
) {
}
