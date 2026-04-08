package com.ticketinglab.android.feature.auth.domain

import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}