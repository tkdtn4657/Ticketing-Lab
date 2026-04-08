package com.ticketinglab.android.core.model.event

import java.time.LocalDateTime

data class EventDetail(
    val eventId: Long,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: LocalDateTime,
    val shows: List<ShowSummary>,
)

data class ShowSummary(
    val showId: Long,
    val startAt: LocalDateTime,
    val status: String,
    val venueId: Long,
)