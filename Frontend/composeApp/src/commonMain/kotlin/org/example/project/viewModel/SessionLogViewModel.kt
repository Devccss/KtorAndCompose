package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.WeeklySessionMetricDto
import org.example.project.repository.SessionLogRepo

data class SessionLogUiState(
    val weeklyMetrics: List<WeeklySessionMetricDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SessionLogViewModel(
    private val repo: SessionLogRepo
) : ViewModel(), ScreenModel {

    private val _state = MutableStateFlow(SessionLogUiState(isLoading = true))
    val state: StateFlow<SessionLogUiState> = _state

    init {
        loadWeeklyMetrics()
    }

    fun loadWeeklyMetrics(fromDate: String? = null, toDate: String? = null) {
        launchCatching(
            block = { repo.getWeeklyMetrics(fromDate = fromDate, toDate = toDate) },
            onSuccess = { metrics ->
                _state.value = _state.value.copy(weeklyMetrics = metrics)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, weeklyMetrics = emptyList())
            }
        )
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            onSuccess(block())
        } catch (e: Exception) {
            onError(e)
        } finally {
            _state.value = _state.value.copy(isLoading = false)
        }
    }
}

