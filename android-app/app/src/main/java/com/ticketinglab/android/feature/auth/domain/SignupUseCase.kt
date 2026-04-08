package com.ticketinglab.android.feature.auth.domain

import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.signup(email, password)
    }
}