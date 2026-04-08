package com.ticketinglab.android.feature.show.domain

import javax.inject.Inject

class GetShowAvailabilityUseCase @Inject constructor(
    private val repository: ShowRepository,
) {
    suspend operator fun invoke(showId: Long) = repository.getShowAvailability(showId)
}