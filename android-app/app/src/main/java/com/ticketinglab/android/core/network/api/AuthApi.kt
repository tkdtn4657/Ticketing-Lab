package com.ticketinglab.android.core.network.api

import com.ticketinglab.android.core.network.dto.auth.CurrentUserDto
import com.ticketinglab.android.core.network.dto.auth.LoginRequestDto
import com.ticketinglab.android.core.network.dto.auth.RefreshTokenRequestDto
import com.ticketinglab.android.core.network.dto.auth.SignupRequestDto
import com.ticketinglab.android.core.network.dto.auth.SignupResponseDto
import com.ticketinglab.android.core.network.dto.auth.TokenPairDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): SignupResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenPairDto

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): TokenPairDto

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto)

    @GET("api/auth/me")
    suspend fun me(): CurrentUserDto
}