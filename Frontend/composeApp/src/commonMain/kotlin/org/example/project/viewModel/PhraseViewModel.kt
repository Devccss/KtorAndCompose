package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.PhraseDto
import org.example.project.dtos.CreatePhraseDto
import org.example.project.dtos.OrderPhraseDto
import org.example.project.repository.PhraseRepository

data class PhraseUiState(
    val phrases: List<PhraseDto> = emptyList(),
    val currentPhrase: PhraseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PhraseViewModel(private val repo: PhraseRepository) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(PhraseUiState(isLoading = true))
    val state: StateFlow<PhraseUiState> = _state

    init {
        loadPhrases()
    }

    private fun loadPhrases() {
        launchCatching(
            block = { repo.getAllPhrases() },
            onSuccess = { phrases ->
                _state.value = _state.value.copy(
                    phrases = phrases,
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

    fun getPhraseById(id: Int) {
        launchCatching(
            block = { repo.getPhraseById(id) },
            onSuccess = { phrase ->
                _state.value = _state.value.copy(
                    currentPhrase = phrase,
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

    fun createPhrase(participantId: Int, phrase: CreatePhraseDto) {
        launchCatching(
            block = { repo.createPhrase(participantId, phrase) },
            onSuccess = { newPhrase ->
                _state.value = _state.value.copy(
                    phrases = _state.value.phrases + newPhrase
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updatePhrase(id: Int, phrase: CreatePhraseDto) {
        launchCatching(
            block = { repo.updatePhrase(id, phrase) },
            onSuccess = { updatedPhrase ->
                _state.value = _state.value.copy(
                    phrases = _state.value.phrases.map { if (it.id == id) updatedPhrase else it },
                    currentPhrase = if (_state.value.currentPhrase?.id == id) updatedPhrase else _state.value.currentPhrase
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deletePhrase(id: Int) {
        launchCatching(
            block = { repo.deletePhrase(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        phrases = _state.value.phrases.filterNot { it.id == id },
                        currentPhrase = if (_state.value.currentPhrase?.id == id) null else _state.value.currentPhrase
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete phrase")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun orderPhrase(orderDto: OrderPhraseDto) {
        launchCatching(
            block = { repo.orderPhrase(orderDto) },
            onSuccess = { success ->
                success?.let {
                    _state.value = _state.value.copy(error = "Failed to order phrase")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
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
