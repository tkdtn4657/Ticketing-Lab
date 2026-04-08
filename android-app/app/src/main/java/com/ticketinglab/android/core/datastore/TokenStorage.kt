package com.ticketinglab.android.core.datastore

import com.ticketinglab.android.core.model.auth.SessionState
import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    val sessionState: Flow<SessionState>

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(accessToken: String, refreshToken: String)

    suspend fun clear()
}