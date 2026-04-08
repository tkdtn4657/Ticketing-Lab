package com.ticketinglab.android.core.network.dto.event

import com.ticketinglab.android.core.model.event.EventDetail
import com.ticketinglab.android.core.model.event.EventSummary
import com.ticketinglab.android.core.model.event.ShowSummary
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class EventListResponseDto(
    val events: List<EventSummaryDto>,
)

@Serializable
data class EventSummaryDto(
    val eventId: Long,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class EventDetailResponseDto(
    val event: EventInfoDto,
    val shows: List<ShowSummaryDto>,
)

@Serializable
data class EventInfoDto(
    val eventId: Long,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class ShowSummaryDto(
    val showId: Long,
    val startAt: String,
    val status: String,
    val venueId: Long,
)

fun EventSummaryDto.toModel(): EventSummary = EventSummary(
    eventId = eventId,
    title = title,
    description = description,
    status = status,
    createdAt = LocalDateTime.parse(createdAt),
)

fun EventDetailResponseDto.toModel(): EventDetail = EventDetail(
    eventId = event.eventId,
    title = event.title,
    description = event.description,
    status = event.status,
    createdAt = LocalDateTime.parse(event.createdAt),
    shows = shows.map(ShowSummaryDto::toModel),
)

fun ShowSummaryDto.toModel(): ShowSummary = ShowSummary(
    showId = showId,
    startAt = LocalDateTime.parse(startAt),
    status = status,
    venueId = venueId,
)