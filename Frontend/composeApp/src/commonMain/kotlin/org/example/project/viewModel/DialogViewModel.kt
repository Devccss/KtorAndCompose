package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.repository.dialogsRepository.KtorDialogsRepository

data class DialogsUiState(
    val dialogs: List<Dialog> = emptyList(),
    val levels: List<Level> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DialogViewModel(private val repo: KtorDialogsRepository) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(DialogsUiState(isLoading = true))
    val state: StateFlow<DialogsUiState> = _state

    init {
        refresh()
        refreshLevels()
    }

    /** === PUBLIC API === */
    fun addDialog(newDialog: Dialog, idLevel: Int) {
        println("addDialog: Intentando crear diálogo con levelId=$idLevel y datos=$newDialog")
        launchCatching(
            block = {
                val result = repo.createDialog(newDialog, idLevel) as Dialog
                println("addDialog: Diálogo creado exitosamente: $result")
                result
            },
            onSuccess = { added ->
                println("addDialog: onSuccess ejecutado con $added")
                _state.value = _state.value.copy(
                    dialogs = _state.value.dialogs + added
                )
            }
        )
    }

    fun updateDialog(id: Int, updatedDialog: Dialog) {
        launchCatching(
            block = { repo.updateDialog(id, updatedDialog) as Dialog },
            onSuccess = { updated ->
                _state.value = _state.value.copy(
                    dialogs = _state.value.dialogs.map { if (it.id == id) updated else it }
                )
            }
        )
    }

    fun deleteDialog(id: Int) {
        launchCatching(
            block = { repo.deleteDialog(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        dialogs = _state.value.dialogs.filterNot { it.id == id }
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete dialog")
                }
            }
        )
    }

    private fun refresh() {
        launchCatching(
            block = {
                val dialogs = repo.getAllDialogs()
                val levels = repo.getAllLevelsFromDialogsRepo()
                Pair<List<Dialog>, List<Level>>(dialogs, levels)
            },
            onSuccess = { (dialogs, levels) ->
                _state.value = DialogsUiState(dialogs = dialogs, levels = levels)
            }
        )
    }

    private fun refreshLevels() {
        viewModelScope.launch {
            try {
                val levels = repo.getAllLevelsFromDialogsRepo()
                _state.value = _state.value.copy(levels = levels)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
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