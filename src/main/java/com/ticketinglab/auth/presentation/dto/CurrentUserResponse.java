package com.ticketinglab.auth.presentation.dto;

public record CurrentUserResponse(
        Long userId,
        String email,
        String role
) {
}
