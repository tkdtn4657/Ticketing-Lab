package com.ticketinglab.auth.presentation.dto;

public record SignupRequest(
        String email,
        String password
) {
}
