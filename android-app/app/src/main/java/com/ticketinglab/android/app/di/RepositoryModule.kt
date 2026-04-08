package com.ticketinglab.android.app.di

import com.ticketinglab.android.feature.auth.data.AuthRepositoryImpl
import com.ticketinglab.android.feature.auth.domain.AuthRepository
import com.ticketinglab.android.feature.event.data.EventRepositoryImpl
import com.ticketinglab.android.feature.event.domain.EventRepository
import com.ticketinglab.android.feature.show.data.ShowRepositoryImpl
import com.ticketinglab.android.feature.show.domain.ShowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindShowRepository(impl: ShowRepositoryImpl): ShowRepository
}