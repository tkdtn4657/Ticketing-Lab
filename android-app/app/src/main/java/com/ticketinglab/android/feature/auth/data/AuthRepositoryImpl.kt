package com.ticketinglab.android.feature.auth.data

import com.ticketinglab.android.core.datastore.TokenStorage
import com.ticketinglab.android.core.model.auth.SessionState
import com.ticketinglab.android.core.network.api.AuthApi
import com.ticketinglab.android.core.network.dto.auth.LoginRequestDto
import com.ticketinglab.android.core.network.dto.auth.RefreshTokenRequestDto
import com.ticketinglab.android.core.network.dto.auth.SignupRequestDto
import com.ticketinglab.android.feature.auth.domain.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override val sessionState: Flow<SessionState> = tokenStorage.sessionState

    override suspend fun signup(email: String, password: String) {
        authApi.signup(SignupRequestDto(email = email, password = password))
    }

    override suspend fun login(email: String, password: String) {
        val tokenPair = authApi.login(LoginRequestDto(email = email, password = password))
        tokenStorage.saveTokens(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken,
        )
    }

    override suspend fun logout() {
        val refreshToken = tokenStorage.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            runCatching {
                authApi.logout(RefreshTokenRequestDto(refreshToken = refreshToken))
            }
        }
        tokenStorage.clear()
    }
}