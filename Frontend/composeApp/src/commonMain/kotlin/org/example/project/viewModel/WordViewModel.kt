package org.example.project.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateExerciseWordDto
import org.example.project.dtos.CreateWordDto
import org.example.project.dtos.ExerciseWordDto
import org.example.project.dtos.UpdateExerciseWordDto
import org.example.project.dtos.UpdateWordDto
import org.example.project.dtos.WordDto
import org.example.project.repository.UnitRepo
import org.example.project.repository.UserRepo
import org.example.project.repository.WordRepository

data class WordUiState(
    val words: List<WordDto> = emptyList(),
    val exerciseWords: List<ExerciseWordDto> = emptyList(),
    val isLoading: Boolean = false,
    var error: String? = null,
)

class WordViewModel(private val repo: WordRepository) : ViewModel(), ScreenModel {

    private val _state = MutableStateFlow(
        WordUiState(
            isLoading = true,
        )
    )

    val state : StateFlow<WordUiState> = _state

    var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        generalMessage = message
    }

    init {
        getAllWords()
    }

    fun getAllWords() {
        launchCatching(
            block = { repo.getAllWords() },
            onSuccess = { words ->
                val activeWords = words.filter { it.isActive == true }
                _state.value = _state.value.copy(words = activeWords, error = null)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, words = emptyList())
            }
        )
    }
    fun getWordById(id: Int) {
        launchCatching(
            block = { repo.getWordById(id) },
            onSuccess = { word ->
                _state.value = _state.value.copy(words = _state.value.words + word, error = null)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, words = emptyList())
            }
        )
    }
    fun getWordsByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { repo.getExerciseWordsByExerciseId(exerciseId) },
            onSuccess = { exerciseWords ->
                _state.value = _state.value.copy(words = emptyList())
                exerciseWords.forEach {
                    launchCatching(
                        block = { repo.getWordById(it.wordId) },
                        onSuccess = { word ->
                            if (word.isActive == true) {
                                _state.value = _state.value.copy(words = _state.value.words + word)
                            }
                        },
                        onError = {}
                    )
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, words = emptyList())
            }
        )
    }
    fun createWord(exerciseId: Int,dto: CreateWordDto) {
        launchCatching(
            block = { repo.createWord(dto) },
            onSuccess = { word ->
                createExerciseWord(
                    CreateExerciseWordDto(
                        exerciseId = exerciseId,
                        wordId = word.id
                    )
                )

                _state.value = _state.value.copy(words = _state.value.words + word, error = null)

            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }
    fun updateWord(idWord: Int, dto: UpdateWordDto) {
        launchCatching(
            block = { repo.updateWord(idWord, dto) },
            onSuccess = { success ->
                if (success) {
                    getAllWords()
                } else {
                    _state.value = _state.value.copy(error = "Failed to update Word")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }
    fun deleteWord(idWord: Int) {
        launchCatching(
            block = { repo.deleteWord(idWord) },
            onSuccess = { success ->
                if (success) {
                    getAllWords()
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete Word")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }



    //ExerciseWord
    fun getExerciseWordsByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { repo.getExerciseWordsByExerciseId(exerciseId) },
            onSuccess = { exerciseWords ->
                _state.value = _state.value.copy(exerciseWords = exerciseWords, error = null)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, exerciseWords = emptyList())
            }
        )
    }

    fun createExerciseWord(dto: CreateExerciseWordDto) {
        launchCatching(
            block = { repo.createExerciseWord(dto) },
            onSuccess = { exerciseWord ->
                _state.value = _state.value.copy(
                    exerciseWords = _state.value.exerciseWords + exerciseWord,
                    error = null
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updateExerciseWord(idExerciseWord: Int, dto: UpdateExerciseWordDto) {
        launchCatching(
            block = { repo.updateExerciseWord(idExerciseWord, dto) },
            onSuccess = { success ->
                if (success) {
                    dto.exerciseId?.let { getExerciseWordsByExerciseId(it) }
                } else {
                    _state.value = _state.value.copy(error = "Failed to update ExerciseWord")
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