package com.ticketinglab.android.core.common.result

sealed class AppException(message: String) : RuntimeException(message) {
    data class Api(val code: Int, override val message: String) : AppException(message)
    data class Unknown(override val message: String) : AppException(message)
}