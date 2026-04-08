package com.ticketinglab.android.core.network.api

import com.ticketinglab.android.core.network.dto.show.ShowAvailabilityResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ShowApi {

    @GET("api/shows/{showId}/availability")
    suspend fun getShowAvailability(@Path("showId") showId: Long): ShowAvailabilityResponseDto
}