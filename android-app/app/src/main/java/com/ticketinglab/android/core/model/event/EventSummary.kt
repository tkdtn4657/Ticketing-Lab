package com.ticketinglab.android.core.model.event

import java.time.LocalDateTime

data class EventSummary(
    val eventId: Long,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: LocalDateTime,
)