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
import org.example.project.dtos.CreateAlternativeDto
import org.example.project.dtos.CreateQuestionDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.repository.QuestionsRepo

data class QuestionUIState(
    val selectedQuestions: List<QuestionDto> = emptyList(),
    val allQuestions: List<QuestionDto> = emptyList(),
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
        _state.value = _state.value.copy(error = message)
    }

    fun getQuestionsByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { repo.getQuestionsByExerciseId(exerciseId) },
            onSuccess = { questions ->
                _state.value = _state.value.copy(selectedQuestions = questions)
                questions.forEach { getAlternativesByQuestionId(it.id) }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener preguntas por id del ejercicio: ${error.message}", selectedQuestions = emptyList())
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
                        _state.value.copy(error = "Error al cargar todas la preguntas", allQuestions = emptyList())
                }
            )
        }
    }

    fun createQuestion(exerciseId: Int, newQuestion: CreateQuestionDto, onQuestionCreated: (Int) -> Unit = {}) {
        launchCatching(
            block = { repo.createQuestion(exerciseId, newQuestion) },
            onSuccess = { createdQuestion ->
                getQuestionsByExerciseId(exerciseId)
                // Asumimos que createdQuestion tiene un ID.
                onQuestionCreated(createdQuestion.id)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al crear la pregunta: ${error.message}")
            }
        )
    }

    fun createAlternativeForQuestion(questionId: Int, newAlternative: CreateAlternativeDto) {
        launchCatching(
            block = { repo.createAlternative(questionId, newAlternative) },
            onSuccess = {
                if (exerciseId != null) {
                    getQuestionsByExerciseId(exerciseId)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al crear la alternativa: ${error.message}")
            }
        )
    }

    fun updateQuestion(questionId: Int, updatedQuestion: UpdateQuestionDto) {
        launchCatching(
            block = { repo.updateQuestion(questionId, updatedQuestion) },
            onSuccess = {
                if (exerciseId != null) {
                    getQuestionsByExerciseId(exerciseId)
                } else {
                    _state.value =
                        _state.value.copy(error = "No hay ejercicio asociado para actualizar la pregunta")
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al actualizar la pregunta: ${error.message}")
            }
        )
    }


    //Alternatives
    fun getAlternativesByQuestionId(questionId: Int) {
        launchCatching(

            block = { repo.getAlternativesByQuestionId(questionId) },
            onSuccess = { alts ->
                _state.value = _state.value.copy(
                    alternatives = _state.value.alternatives + (questionId to alts)
                )
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener alternativas por id de pregunta: ${error.message}")
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
                    _state.value.copy(error = "Error al actualizar la alternativa: ${error.message}")
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