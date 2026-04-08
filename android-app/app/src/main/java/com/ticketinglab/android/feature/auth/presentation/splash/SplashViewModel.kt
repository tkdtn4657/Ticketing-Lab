package com.ticketinglab.android.feature.auth.presentation.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.core.model.auth.SessionState
import com.ticketinglab.android.feature.auth.domain.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(SplashUiState())
        private set

    private val _events = MutableSharedFlow<SplashEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeSessionUseCase().collect { sessionState ->
                uiState = uiState.copy(isLoading = false)
                when (sessionState) {
                    SessionState.Unknown,
                    SessionState.Anonymous -> _events.emit(SplashEvent.NavigateToLogin)

                    is SessionState.Authenticated -> _events.emit(SplashEvent.NavigateToEvents)
                }
            }
        }
    }
}

data class SplashUiState(
    val isLoading: Boolean = true,
)

sealed interface SplashEvent {
    data object NavigateToLogin : SplashEvent
    data object NavigateToEvents : SplashEvent
}