package com.ticketinglab.android.feature.event.domain

import javax.inject.Inject

class GetEventDetailUseCase @Inject constructor(
    private val repository: EventRepository,
) {
    suspend operator fun invoke(eventId: Long) = repository.getEventDetail(eventId)
}