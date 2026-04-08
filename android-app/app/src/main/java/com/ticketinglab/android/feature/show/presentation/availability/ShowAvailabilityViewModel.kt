package com.ticketinglab.android.feature.show.presentation.availability

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketinglab.android.core.model.show.ShowAvailability
import com.ticketinglab.android.feature.show.domain.GetShowAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ShowAvailabilityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getShowAvailabilityUseCase: GetShowAvailabilityUseCase,
) : ViewModel() {

    private val showId: Long = checkNotNull(savedStateHandle["showId"])

    var uiState by mutableStateOf(ShowAvailabilityUiState(isLoading = true))
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching {
                getShowAvailabilityUseCase(showId)
            }.onSuccess { availability ->
                uiState = uiState.copy(isLoading = false, availability = availability)
            }.onFailure {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "가용성 정보를 불러오지 못했습니다.",
                )
            }
        }
    }
}

data class ShowAvailabilityUiState(
    val isLoading: Boolean = false,
    val availability: ShowAvailability? = null,
    val errorMessage: String? = null,
)