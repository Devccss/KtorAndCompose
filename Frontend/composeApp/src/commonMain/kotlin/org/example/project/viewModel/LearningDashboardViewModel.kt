package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.LearningDashboardDto
import org.example.project.repository.LearningDashboardRepository

data class LearningDashboardState(
    val isLoading: Boolean = false,
    val dashboard: LearningDashboardDto? = null,
    val error: String? = null
)

class LearningDashboardViewModel(
    private val repo: LearningDashboardRepository
) : ViewModel(), ScreenModel {

    private val _state =
        MutableStateFlow(LearningDashboardState())

    val state = _state.asStateFlow()

    fun loadDashboard() {

        
        launchCatching(
            block = { repo.getDashboard() },
            onSuccess = { dashboard ->
                _state.value = _state.value.copy(
                    dashboard = dashboard,
                    isLoading = false,
                    error = null
                )
            },
            onError = { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
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
            _state.value = _state.value.copy(isLoading = false)

        } catch (e: Exception) {
            onError(e)
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}