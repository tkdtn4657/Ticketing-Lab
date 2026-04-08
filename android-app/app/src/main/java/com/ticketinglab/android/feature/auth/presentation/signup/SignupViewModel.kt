package com.ticketinglab.android.feature.auth.presentation.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.feature.auth.domain.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signupUseCase: SignupUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(SignupUiState())
        private set

    private val _events = MutableSharedFlow<SignupEvent>()
    val events = _events.asSharedFlow()

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null, completed = false)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null, completed = false)
    }

    fun signup() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(errorMessage = "이메일과 비밀번호를 모두 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                signupUseCase(uiState.email.trim(), uiState.password)
            }.onSuccess {
                uiState = uiState.copy(completed = true)
            }.onFailure {
                uiState = uiState.copy(errorMessage = it.message ?: "회원가입에 실패했습니다.")
            }
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun backToLogin() {
        viewModelScope.launch {
            _events.emit(SignupEvent.NavigateToLogin)
        }
    }
}

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val completed: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SignupEvent {
    data object NavigateToLogin : SignupEvent
}