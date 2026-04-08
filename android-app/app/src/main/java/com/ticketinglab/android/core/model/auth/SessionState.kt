package com.ticketinglab.android.core.model.auth

sealed interface SessionState {
    data object Unknown : SessionState
    data object Anonymous : SessionState
    data class Authenticated(
        val accessToken: String,
        val refreshToken: String,
    ) : SessionState
}