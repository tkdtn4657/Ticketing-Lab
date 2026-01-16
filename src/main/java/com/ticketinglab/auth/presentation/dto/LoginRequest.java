package com.ticketinglab.auth.presentation.dto;

public record LoginRequest(
        String email,
        String password
) {
}
