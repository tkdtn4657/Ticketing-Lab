package com.ticketinglab.android.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ticketinglab.android.BuildConfig
import com.ticketinglab.android.core.datastore.SessionDataStore
import com.ticketinglab.android.core.datastore.TokenStorage
import com.ticketinglab.android.core.network.api.AuthApi
import com.ticketinglab.android.core.network.api.EventApi
import com.ticketinglab.android.core.network.api.ShowApi
import com.ticketinglab.android.core.network.interceptor.AuthHeaderInterceptor
import com.ticketinglab.android.core.network.interceptor.RefreshTokenAuthenticator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

private const val SESSION_DATASTORE_FILE = "ticketing_session.preferences_pb"

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: SessionDataStore): TokenStorage
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(SESSION_DATASTORE_FILE) },
        )
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    @Named("baseOkHttp")
    fun provideBaseOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        refreshTokenAuthenticator: RefreshTokenAuthenticator,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authHeaderInterceptor)
            .authenticator(refreshTokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @Named("refreshOkHttp")
    fun provideRefreshOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("appRetrofit")
    fun provideAppRetrofit(
        json: Json,
        @Named("baseOkHttp") okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("refreshRetrofit")
    fun provideRefreshRetrofit(
        json: Json,
        @Named("refreshOkHttp") okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("appRetrofit") retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    @Named("refreshAuthApi")
    fun provideRefreshAuthApi(@Named("refreshRetrofit") retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideEventApi(@Named("appRetrofit") retrofit: Retrofit): EventApi = retrofit.create(EventApi::class.java)

    @Provides
    @Singleton
    fun provideShowApi(@Named("appRetrofit") retrofit: Retrofit): ShowApi = retrofit.create(ShowApi::class.java)
}