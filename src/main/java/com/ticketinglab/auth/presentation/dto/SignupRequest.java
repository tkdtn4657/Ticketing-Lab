package com.ticketinglab.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
