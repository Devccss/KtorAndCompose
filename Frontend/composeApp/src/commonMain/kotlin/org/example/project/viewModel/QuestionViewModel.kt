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
import kotlinx.coroutines.flow.update
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
                val activeQuestions = questions.filter { it.isActive == true }
                _state.value = _state.value.copy(selectedQuestions = activeQuestions)
                activeQuestions.forEach { getAlternativesByQuestionId(it.id) }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener preguntas por id del ejercicio: ${error.message}", selectedQuestions = emptyList())
            }
        )
    }

    fun createQuestion(exerciseContent: Int, newQuestion: CreateQuestionDto, onQuestionCreated: (Int) -> Unit = {}) {
        launchCatching(
            block = { repo.createQuestion(exerciseContent, newQuestion) },
            onSuccess = { createdQuestion ->
                _state.update { currentState ->
                    currentState.copy(
                        selectedQuestions = currentState.selectedQuestions + createdQuestion
                    )
                }
                onQuestionCreated(createdQuestion.id)
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al crear la pregunta: ${error.message}") }
            }
        )
    }

    fun updateQuestion(questionId: Int, updatedQuestion: UpdateQuestionDto) {
        launchCatching(
            block = { repo.updateQuestion(questionId, updatedQuestion) },
            onSuccess = {
                // Actualización OPTIMISTA / MANUAL
                _state.update { currentState ->
                    val currentList = currentState.selectedQuestions
                    val newList = currentList.map { q ->
                        if (q.id == questionId) {
                            q.copy(
                                exerciseContentId = updatedQuestion.exerciseContentId ?: q.exerciseContentId,
                                questionText = updatedQuestion.questionText ?: q.questionText,
                                orderQuestion = updatedQuestion.orderQuestion ?: q.orderQuestion,
                                isActive = updatedQuestion.isActive ?: q.isActive
                            )
                        } else {
                            q
                        }
                    }
                    currentState.copy(selectedQuestions = newList)
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al actualizar la pregunta: ${error.message}") }
            }
        )
    }

    fun deleteQuestion(questionId: Int) {
        launchCatching(
            block = { repo.deleteQuestion(questionId) },
            onSuccess = {
                _state.update { currentState ->
                    currentState.copy(
                        selectedQuestions = currentState.selectedQuestions.filterNot { it.id == questionId },
                        alternatives = currentState.alternatives - questionId
                    )
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al eliminar la pregunta: ${error.message}") }
            }
        )
    }


    //Alternatives
    fun getAlternativesByQuestionId(questionId: Int) {
        launchCatching(

            block = { repo.getAlternativesByQuestionId(questionId) },
            onSuccess = { alts ->
                _state.update { currentState ->
                    currentState.copy(
                        alternatives = currentState.alternatives + (questionId to alts)
                    )
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al obtener alternativas: ${error.message}") }
            }
        )
    }
    fun createAlternativeForQuestion(questionId: Int, newAlternative: CreateAlternativeDto) {
        launchCatching(
            block = { repo.createAlternative(questionId, newAlternative) },
            onSuccess = {alt ->
                // Actualizar estado local de forma atómica para evitar race conditions
                _state.update { currentState ->
                    val currentAlts = currentState.alternatives[questionId] ?: emptyList()
                    val newAlts = currentAlts + alt
                    currentState.copy(
                        alternatives = currentState.alternatives + (questionId to newAlts)
                    )
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al crear la alternativa: ${error.message}") }
            }
        )
    }

    fun updateAlternativesForQuestion(alternativeId: Int, alternative: UpdateAlternativeDto) {
        launchCatching(
            block = { repo.updateAlternative(alternativeId, alternative) },
            onSuccess = {
                _state.update { currentState ->
                    val currentMap = currentState.alternatives.toMutableMap()
                    
                    for ((qId, altList) in currentMap) {
                        val index = altList.indexOfFirst { it.id == alternativeId }
                        if (index != -1) {
                            val currentAlt = altList[index]
                            val updatedAlt = currentAlt.copy(
                                text = alternative.text ?: currentAlt.text,
                                isCorrect = alternative.isCorrect ?: currentAlt.isCorrect
                            )
                            val newList = altList.toMutableList()
                            newList[index] = updatedAlt
                            currentMap[qId] = newList
                            break
                        }
                    }
                    currentState.copy(alternatives = currentMap)
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al actualizar la alternativa: ${error.message}") }
            }
        )
    }

    fun deleteAlternative(alternativeId: Int) {
        launchCatching(
            block = { repo.deleteAlternative(alternativeId) },
            onSuccess = {
                // Actualización OPTIMISTA local
                _state.update { currentState ->
                    val currentMap = currentState.alternatives.toMutableMap()
                    var foundKey: Int? = null

                    for ((qId, list) in currentMap) {
                        if (list.any { it.id == alternativeId }) {
                            foundKey = qId
                            break
                        }
                    }

                    if (foundKey != null) {
                        val newList = currentMap[foundKey]!!.filter { it.id != alternativeId }
                        currentMap[foundKey] = newList
                    }
                    currentState.copy(alternatives = currentMap)
                }
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al eliminar la alternativa: ${error.message}") }
            }
        )
    }


    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val result = block()
            onSuccess(result)
            _state.update { it.copy(isLoading = false) }

        } catch (e: Exception) {
            onError(e)
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}