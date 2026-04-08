package com.ticketinglab.android.core.network.interceptor

import com.ticketinglab.android.core.datastore.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthHeaderInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenStorage.getAccessToken() }
        val request = if (accessToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        return chain.proceed(request)
    }
}