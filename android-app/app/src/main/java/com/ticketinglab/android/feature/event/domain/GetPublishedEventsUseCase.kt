package com.ticketinglab.android.feature.event.domain

import javax.inject.Inject

class GetPublishedEventsUseCase @Inject constructor(
    private val repository: EventRepository,
) {
    suspend operator fun invoke() = repository.getPublishedEvents()
}