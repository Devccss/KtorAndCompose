package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.models.Level
import org.example.project.repository.LevelRepository

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
    fun addLevel(newLevel: Level) {
        launchCatching(
            block = { repo.addLevel(newLevel) },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    levels = _state.value.levels + added
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
        launchCatching( { repo.deleteLevel(levelId) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        levels = _state.value.levels.filterNot { it.id == levelId }
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete level")
                }
            }
        )
    }


    fun saveEdits(edited: Level) {
        val id = edited.id ?: return
        launchCatching(
            block = { repo.updateLevel(id, edited) },
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
