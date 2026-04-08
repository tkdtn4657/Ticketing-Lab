package com.ticketinglab.android.feature.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.feature.auth.domain.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun login() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(errorMessage = "이메일과 비밀번호를 모두 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                loginUseCase(uiState.email.trim(), uiState.password)
            }.onSuccess {
                _events.emit(LoginEvent.NavigateToEvents)
            }.onFailure {
                uiState = uiState.copy(errorMessage = it.message ?: "로그인에 실패했습니다.")
            }
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun openSignup() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToSignup)
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginEvent {
    data object NavigateToEvents : LoginEvent
    data object NavigateToSignup : LoginEvent
}