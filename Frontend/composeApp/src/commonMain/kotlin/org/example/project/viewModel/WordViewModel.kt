package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.WordDto
import org.example.project.dtos.CreateWordDto
import org.example.project.repository.WordRepository

data class WordUiState(
    val words: List<WordDto> = emptyList(),
    val currentWord: WordDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WordViewModel(private val repo: WordRepository) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(WordUiState(isLoading = true))
    val state: StateFlow<WordUiState> = _state

    init {
        loadWords()
    }

    fun loadWords() {
        launchCatching(
            block = { repo.getAllWords() },
            onSuccess = { words ->
                _state.value = _state.value.copy(
                    words = words,
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

    fun getWordById(id: Int) {
        launchCatching(
            block = { repo.getWordById(id) },
            onSuccess = { word ->
                _state.value = _state.value.copy(
                    currentWord = word,
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

    fun getWordsByPhraseId(phraseId: Int) {
        launchCatching(
            block = { repo.getWordsByPhraseId(phraseId) },
            onSuccess = { words ->
                _state.value = _state.value.copy(
                    words = words,
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

    fun createWord(word: CreateWordDto) {
        launchCatching(
            block = { repo.createWord(word) },
            onSuccess = { newWord ->
                _state.value = _state.value.copy(
                    words = _state.value.words + newWord
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updateWord(id: Int, word: CreateWordDto) {
        launchCatching(
            block = { repo.updateWord(id, word) },
            onSuccess = { updatedWord ->
                _state.value = _state.value.copy(
                    words = _state.value.words.map { if (it.id == id) updatedWord else it },
                    currentWord = if (_state.value.currentWord?.id == id) updatedWord else _state.value.currentWord
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deleteWord(id: Int) {
        launchCatching(
            block = { repo.deleteWord(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        words = _state.value.words.filterNot { it.id == id },
                        currentWord = if (_state.value.currentWord?.id == id) null else _state.value.currentWord
                    )
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete word")
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
