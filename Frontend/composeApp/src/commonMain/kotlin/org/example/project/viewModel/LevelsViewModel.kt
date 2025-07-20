package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.models.Level
import org.example.project.repository.levelRepository.LevelRepository

data class LevelsUiState(
    val levels: List<Level> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LevelsViewModel(private val repo: LevelRepository) : ViewModel(), ScreenModel {

    private val _state = MutableStateFlow(LevelsUiState(isLoading = true))
    val state: StateFlow<LevelsUiState> = _state

    init { refresh() }

    /** === PUBLIC API === */
    fun addLevel(newLevel: Level, beforeId: Int? = null, afterId: Int? = null) {
        launchCatching(
            block = { repo.addLevel(newLevel, beforeId, afterId) },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    levels = (_state.value.levels + added).sortedBy { it.orderLevel
                    }
                )
            }
        )
    }

    fun refresh() {
        launchCatching(
            block = { repo.getAllLevels() },
            onSuccess = { levels -> _state.value = LevelsUiState(levels = levels) }
        )
    }

    fun delete(levelId: Int) {
        launchCatching(
            block = { repo.deleteLevel(levelId) },
            onSuccess = { result ->
                result.fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            levels = _state.value.levels.filterNot { it.id == levelId }
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(error = error.message)
                    }
                )
            }
        )
    }


    fun saveEdits(edited: Level, beforeId: Int? = null, afterId: Int? = null) {
        val id = edited.id ?: return
        launchCatching(
            block = { repo.updateLevel(id, edited, beforeId, afterId) },
            onSuccess = { updated ->
                _state.value = _state.value.copy(
                    levels = _state.value.levels.map { if (it.id == updated.id) updated else it }
                )
            }
        )
    }

    /** === HELPERS === */

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            onSuccess(block())
            _state.value = _state.value.copy(isLoading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}
