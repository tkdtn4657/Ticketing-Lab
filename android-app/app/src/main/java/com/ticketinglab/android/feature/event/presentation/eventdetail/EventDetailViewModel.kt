package com.ticketinglab.android.feature.event.presentation.eventdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.core.model.event.EventDetail
import com.ticketinglab.android.feature.event.domain.GetEventDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventDetailUseCase: GetEventDetailUseCase,
) : ViewModel() {

    private val eventId: Long = checkNotNull(savedStateHandle["eventId"])

    var uiState by mutableStateOf(EventDetailUiState(isLoading = true))
        private set

    private val _events = MutableSharedFlow<EventDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                getEventDetailUseCase(eventId)
            }.onSuccess { detail ->
                uiState = uiState.copy(isLoading = false, eventDetail = detail)
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "이벤트 상세를 불러오지 못했습니다.",
                )
            }
        }
    }

    fun openShow(showId: Long) {
        viewModelScope.launch {
            _events.emit(EventDetailEvent.OpenShowAvailability(showId))
        }
    }
}

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val eventDetail: EventDetail? = null,
    val errorMessage: String? = null,
)

sealed interface EventDetailEvent {
    data class OpenShowAvailability(val showId: Long) : EventDetailEvent
}