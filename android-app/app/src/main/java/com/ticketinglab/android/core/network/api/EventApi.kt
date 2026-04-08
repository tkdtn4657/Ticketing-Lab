package com.ticketinglab.android.core.network.api

import com.ticketinglab.android.core.network.dto.event.EventDetailResponseDto
import com.ticketinglab.android.core.network.dto.event.EventListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {

    @GET("api/events")
    suspend fun getEvents(@Query("status") status: String? = null): EventListResponseDto

    @GET("api/events/{eventId}")
    suspend fun getEventDetail(@Path("eventId") eventId: Long): EventDetailResponseDto
}