package com.ticketinglab.android.core.network.interceptor

import com.ticketinglab.android.core.datastore.TokenStorage
import com.ticketinglab.android.core.network.api.AuthApi
import com.ticketinglab.android.core.network.dto.auth.RefreshTokenRequestDto
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class RefreshTokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    @Named("refreshAuthApi") private val refreshAuthApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking { tokenStorage.getRefreshToken() } ?: return null

        synchronized(this) {
            val updatedAccessToken = runBlocking { tokenStorage.getAccessToken() }
            val failedRequestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            if (!updatedAccessToken.isNullOrBlank() && updatedAccessToken != failedRequestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $updatedAccessToken")
                    .build()
            }

            val refreshedTokenPair = runBlocking {
                runCatching {
                    refreshAuthApi.refresh(RefreshTokenRequestDto(refreshToken))
                }.getOrNull()
            } ?: run {
                runBlocking { tokenStorage.clear() }
                return null
            }

            runBlocking {
                tokenStorage.saveTokens(
                    accessToken = refreshedTokenPair.accessToken,
                    refreshToken = refreshedTokenPair.refreshToken,
                )
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshedTokenPair.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}