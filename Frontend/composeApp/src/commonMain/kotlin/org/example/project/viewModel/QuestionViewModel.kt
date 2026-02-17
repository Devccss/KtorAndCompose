package org.example.project.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.AlternativesDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.repository.QuestionsRepo

data class QuestionUIState(
    val selectedQuestions: List<QuestionDto> = emptyList(),
    val allQuestions: List<QuestionDto> = emptyList(),
    // Agregamos el mapa: ID Pregunta -> Lista de Alternativas
    val alternatives: Map<Int, List<AlternativesDto>> = emptyMap(),
    var error: String? = null,
    val isLoading: Boolean = false,
)

class QuestionViewModel(private val repo: QuestionsRepo, private val exerciseId: Int? = null) :
    ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(
        QuestionUIState(
            selectedQuestions = emptyList(),
            allQuestions = emptyList(),
            alternatives = emptyMap()
        )
    )
    val state: StateFlow<QuestionUIState> = _state

    private var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        generalMessage = message
    }
    private fun getQuestionsByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { repo.getQuestionsByExerciseId(exerciseId) },
            onSuccess = { questions ->
                _state.value = _state.value.copy(selectedQuestions = questions)
                questions.forEach { getAlternativesByQuestionId(it.id) }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = error.message, selectedQuestions = emptyList())
            }
        )
    }

    init {
        if (exerciseId != null) {
        getQuestionsByExerciseId(exerciseId)

        } else {
            launchCatching(
                block = { repo.getAllQuestions() },
                onSuccess = { question ->

                    _state.value = _state.value.copy(allQuestions = question)
                },
                onError = { error ->
                    _state.value =
                        _state.value.copy(error = error.message, allQuestions = emptyList())
                }
            )
        }
    }



    fun updateQuestion(questionId: Int, updatedQuestion: UpdateQuestionDto) {
        launchCatching(
            block = { repo.updateQuestion(questionId, updatedQuestion) },
            onSuccess = { question ->
                if (exerciseId != null) {
                    getQuestionsByExerciseId(exerciseId)
                } else {
                    // Si no hay un exerciseId específico, actualizamos la lista general
                    val updatedList = _state.value.allQuestions.map {
                        if (it.id == questionId) question else it
                    }
                    _state.value = _state.value.copy(allQuestions = updatedList)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = error.message)
            }
        )
    }


    //Alternatives
    private fun getAlternativesByQuestionId(questionId: Int) {
        launchCatching(

            block = { repo.getAlternativesByQuestionId(questionId) },
            onSuccess = { alts ->
                _state.value = _state.value.copy(
                    alternatives = _state.value.alternatives + (questionId to alts)
                )
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = error.message)
            }
        )
    }


    fun updateAlternativesForQuestion(alternativeId: Int, alternative: UpdateAlternativeDto) {
        launchCatching(
            block = { repo.updateAlternative(alternativeId, alternative) },
            onSuccess = {
                if (exerciseId != null) {
                    getQuestionsByExerciseId(exerciseId)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = error.message)
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