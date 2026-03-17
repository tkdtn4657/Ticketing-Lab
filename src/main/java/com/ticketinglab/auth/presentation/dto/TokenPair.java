package com.ticketinglab.auth.presentation.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
