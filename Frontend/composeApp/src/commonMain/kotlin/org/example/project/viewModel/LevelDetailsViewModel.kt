package org.example.project.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.repository.DialogsRepository
import org.example.project.repository.KtorLevelRepository

data class LevelDetailsUiState(
    val level: Level? = null,
    val dialogs: List<Dialog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class LevelDetailsViewModel(
    private val levelId: Int?,
    private val levelRepository: KtorLevelRepository,
    private val dialogsRepository: DialogsRepository
) : ScreenModel, ViewModel() {

    private val _state = MutableStateFlow(LevelDetailsUiState(isLoading = true))
    var state: StateFlow<LevelDetailsUiState> = _state

    init {
        loadLevelDetails()
    }

    private fun loadLevelDetails() {
        CoroutineScope(Dispatchers.Default).launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val level = levelId?.let { levelRepository.getLevelById(it) }
                val dialogs = levelId?.let { dialogsRepository.getAllDialogsOfLevelId(it) } ?: emptyList()
                _state.value = _state.value.copy(
                    level = level,
                    dialogs = dialogs,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun saveEdits(edited: Level, beforeId: Int? = null, afterId: Int? = null) {
        val id = edited.id ?: return
        launchCatching(
            block = { levelRepository.updateLevel(id, edited, beforeId, afterId) },
            onSuccess = { updated ->
                _state.value = _state.value.copy(
                    level = updated
                )
            }
        )
    }

    fun delete(levelId: Int) {
        launchCatching(
            block = { levelRepository.deleteLevel(levelId) },
            onSuccess = {
                _state.value = _state.value.copy(
                    level = null
                )
            }
        )
    }

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
