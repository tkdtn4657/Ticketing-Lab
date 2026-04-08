package com.ticketinglab.android.feature.show.data

import com.ticketinglab.android.core.network.api.ShowApi
import com.ticketinglab.android.core.network.dto.show.toModel
import com.ticketinglab.android.feature.show.domain.ShowRepository
import javax.inject.Inject

class ShowRepositoryImpl @Inject constructor(
    private val showApi: ShowApi,
) : ShowRepository {

    override suspend fun getShowAvailability(showId: Long) = showApi.getShowAvailability(showId).toModel()
}