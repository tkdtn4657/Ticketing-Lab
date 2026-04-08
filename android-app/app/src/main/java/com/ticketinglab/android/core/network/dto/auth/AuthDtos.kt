package com.ticketinglab.android.core.network.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class SignupRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class SignupResponseDto(
    val userId: Long,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class CurrentUserDto(
    val userId: Long,
    val email: String,
    val role: String,
)