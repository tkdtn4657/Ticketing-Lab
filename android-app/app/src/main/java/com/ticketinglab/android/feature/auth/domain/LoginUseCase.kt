package com.ticketinglab.android.feature.auth.domain

import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.login(email, password)
    }
}