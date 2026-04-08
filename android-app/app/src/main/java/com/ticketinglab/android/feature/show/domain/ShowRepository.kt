package com.ticketinglab.android.feature.show.domain

import com.ticketinglab.android.core.model.show.ShowAvailability

interface ShowRepository {
    suspend fun getShowAvailability(showId: Long): ShowAvailability
}