package com.ticketinglab.android.feature.event.data

import com.ticketinglab.android.core.network.api.EventApi
import com.ticketinglab.android.core.network.dto.event.toModel
import com.ticketinglab.android.feature.event.domain.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
) : EventRepository {

    override suspend fun getPublishedEvents() = eventApi.getEvents(status = "PUBLISHED")
        .events
        .map { it.toModel() }

    override suspend fun getEventDetail(eventId: Long) = eventApi.getEventDetail(eventId).toModel()
}