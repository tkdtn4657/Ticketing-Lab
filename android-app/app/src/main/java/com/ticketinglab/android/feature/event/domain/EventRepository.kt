package com.ticketinglab.android.feature.event.domain

import com.ticketinglab.android.core.model.event.EventDetail
import com.ticketinglab.android.core.model.event.EventSummary

interface EventRepository {
    suspend fun getPublishedEvents(): List<EventSummary>

    suspend fun getEventDetail(eventId: Long): EventDetail
}