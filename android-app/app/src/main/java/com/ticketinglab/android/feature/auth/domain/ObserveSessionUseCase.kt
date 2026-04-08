package com.ticketinglab.android.feature.auth.domain

import com.ticketinglab.android.core.model.auth.SessionState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<SessionState> = repository.sessionState
}