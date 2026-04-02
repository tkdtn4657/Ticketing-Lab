package com.ticketinglab.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReservationRequest(
        @NotBlank String holdId
) {
}