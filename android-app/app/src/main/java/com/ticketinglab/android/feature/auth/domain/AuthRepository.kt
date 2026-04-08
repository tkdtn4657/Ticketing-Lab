package com.ticketinglab.android.feature.auth.domain

import com.ticketinglab.android.core.model.auth.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionState: Flow<SessionState>

    suspend fun signup(email: String, password: String)

    suspend fun login(email: String, password: String)

    suspend fun logout()
}