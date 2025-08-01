package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.PhraseWordDto
import org.example.project.dtos.CreatePhraseWordDto
import org.example.project.repository.PhraseWordRepository

data class PhraseWordUiState(
    val phraseWords: List<PhraseWordDto> = emptyList(),
    val currentPhraseWord: PhraseWordDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PhraseWordViewModel(private val repo: PhraseWordRepository) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(PhraseWordUiState(isLoading = true))
    val state: StateFlow<PhraseWordUiState> = _state

    init {
        loadPhraseWords()
    }

    fun loadPhraseWords() {
        launchCatching(
            block = { repo.getAllPhraseWords() },
            onSuccess = { phraseWords ->
                _state.value = _state.value.copy(
                    phraseWords = phraseWords,
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

    fun getPhraseWordById(id: Int) {
        launchCatching(
            block = { repo.getPhraseWordById(id) },
            onSuccess = { phraseWord ->
                _state.value = _state.value.copy(
                    currentPhraseWord = phraseWord,
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

    fun getPhraseWordsByPhraseId(phraseId: Int) {
        launchCatching(
            block = { repo.getPhraseWordsByPhraseId(phraseId) },
            onSuccess = { phraseWords ->
                _state.value = _state.value.copy(
                    phraseWords = phraseWords,
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

    fun getPhraseWordsByWordId(wordId: Int) {
        launchCatching(
            block = { repo.getPhraseWordsByWordId(wordId) },
            onSuccess = { phraseWords ->
                _state.value = _state.value.copy(
                    phraseWords = phraseWords,
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

    fun createPhraseWord(dto: CreatePhraseWordDto) {
        launchCatching(
            block = { repo.createPhraseWord(dto) },
            onSuccess = { newPhraseWord ->
                _state.value = _state.value.copy(
                    phraseWords = _state.value.phraseWords + newPhraseWord
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updatePhraseWord(id: Int, dto: CreatePhraseWordDto) {
        launchCatching(
            block = { repo.updatePhraseWord(id, dto) },
            onSuccess = { updatedPhraseWord ->
                _state.value = _state.value.copy(
                    phraseWords = _state.value.phraseWords.map { if (it.id == id) updatedPhraseWord else it },
                    currentPhraseWord = if (_state.value.currentPhraseWord?.id == id) updatedPhraseWord else _state.value.currentPhraseWord
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deletePhraseWord(id: Int) {
        launchCatching(
            block = { repo.deletePhraseWord(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        phraseWords = _state.value.phraseWords.filterNot { it.id == id },
                        currentPhraseWord = if (_state.value.currentPhraseWord?.id == id) null else _state.value.currentPhraseWord
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete phraseWord")
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
