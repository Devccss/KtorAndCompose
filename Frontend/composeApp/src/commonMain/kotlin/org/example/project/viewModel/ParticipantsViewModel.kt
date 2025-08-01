package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateParticipantDTO
import org.example.project.dtos.DialogParticipantDTO

import org.example.project.repository.ParticipantsRepository

data class ParticipantsUiState(
    val participants: List<DialogParticipantDTO> = emptyList(),
    val currentParticipant: DialogParticipantDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ParticipantsViewModel(private val repo: ParticipantsRepository, idDialog: Int) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(ParticipantsUiState(isLoading = true))
    val state: StateFlow<ParticipantsUiState> = _state

    init {
        loadParticipants(idDialog)
    }

    private fun loadParticipants(dialogId: Int) {
        launchCatching(
            block = { repo.getParticipantsByDialogId(dialogId) },
            onSuccess = { participant ->
                _state.value = _state.value.copy(
                    participants = participant,
                    isLoading = false
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
        )
    }

    fun addParticipant(newParticipant: CreateParticipantDTO, idDialog: Int) {
        launchCatching(
            block = { repo.createParticipant(idDialog,newParticipant) },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    participants = _state.value.participants + added
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun getParticipantById(id: Int) {
        launchCatching(
            block = { repo.getParticipantById(id) },
            onSuccess = { participant ->
                _state.value = _state.value.copy(
                    currentParticipant = participant,
                    isLoading = false
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
        )
    }

    fun updateParticipant(id: Int, updatedParticipant: CreateParticipantDTO) {
        launchCatching(
            block = { repo.updateParticipant(id, updatedParticipant) },
            onSuccess = { updated ->
                _state.value = _state.value.copy(
                    participants = _state.value.participants.map { if (it.id == id) updated else it },
                    currentParticipant = if (_state.value.currentParticipant?.id == id) updated else _state.value.currentParticipant
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deleteParticipant(id: Int) {
        launchCatching(
            block = { repo.deleteParticipant(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        participants = _state.value.participants.filterNot { it.id == id },
                        currentParticipant = if (_state.value.currentParticipant?.id == id) null else _state.value.currentParticipant
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete participant")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun logout() {
        _state.value = _state.value.copy(currentParticipant = null)
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
