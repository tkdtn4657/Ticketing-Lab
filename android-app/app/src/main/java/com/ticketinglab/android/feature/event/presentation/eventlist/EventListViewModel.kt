package com.ticketinglab.android.feature.event.presentation.eventlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.core.model.event.EventSummary
import com.ticketinglab.android.feature.event.domain.GetPublishedEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getPublishedEventsUseCase: GetPublishedEventsUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(EventListUiState(isLoading = true))
        private set

    private val _events = MutableSharedFlow<EventListEvent>()
    val events = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                getPublishedEventsUseCase()
            }.onSuccess { items ->
                uiState = uiState.copy(isLoading = false, events = items)
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "이벤트를 불러오지 못했습니다.",
                )
            }
        }
    }

    fun openEvent(eventId: Long) {
        viewModelScope.launch {
            _events.emit(EventListEvent.OpenEventDetail(eventId))
        }
    }

    fun openTickets() {
        viewModelScope.launch {
            _events.emit(EventListEvent.OpenTicketsComingSoon)
        }
    }
}

data class EventListUiState(
    val isLoading: Boolean = false,
    val events: List<EventSummary> = emptyList(),
    val errorMessage: String? = null,
)

sealed interface EventListEvent {
    data class OpenEventDetail(val eventId: Long) : EventListEvent
    data object OpenTicketsComingSoon : EventListEvent
}