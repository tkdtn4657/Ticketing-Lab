package com.ticketinglab.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ticketinglab.android.core.model.auth.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {

    override val sessionState: Flow<SessionState> = dataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]

        when {
            accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() -> SessionState.Anonymous
            else -> SessionState.Authenticated(accessToken, refreshToken)
        }
    }

    override suspend fun getAccessToken(): String? = dataStore.data
        .map { it[ACCESS_TOKEN] }
        .firstOrNull()

    override suspend fun getRefreshToken(): String? = dataStore.data
        .map { it[REFRESH_TOKEN] }
        .firstOrNull()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}