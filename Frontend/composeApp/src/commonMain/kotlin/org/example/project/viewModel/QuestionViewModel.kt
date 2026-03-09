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

    fun createQuestion(exerciseId: Int, newQuestion: CreateQuestionDto, onQuestionCreated: (Int) -> Unit = {}) {
        launchCatching(
            block = { repo.createQuestion(exerciseId, newQuestion) },
            onSuccess = { createdQuestion ->
                _state.value = _state.value.copy(
                    selectedQuestions = _state.value.selectedQuestions + createdQuestion
                )
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
            onSuccess = {alt ->
                // Actualizar estado local agregando la nueva alternativa a la lista existente
                val currentAlts = _state.value.alternatives[questionId] ?: emptyList()
                val newAlts = currentAlts + alt
                _state.value = _state.value.copy(
                    alternatives = _state.value.alternatives + (questionId to newAlts)
                )
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
                // Actualización OPTIMISTA / MANUAL:
                // Como el backend devuelve Boolean (o no devuelve el DTO actualizado al fallar deserialización),
                // actualizamos el estado local con los datos que acabamos de enviar.
                // Esto evita recargar datos antiguos por condiciones de carrera.

                val currentList = _state.value.selectedQuestions
                val newList = currentList.map { q ->
                    if (q.id == questionId) {
                        q.copy(
                            textContent = updatedQuestion.textContent ?: q.textContent,
                            grammarExplanation = updatedQuestion.grammarExplanation ?: q.grammarExplanation,
                            questionText = updatedQuestion.questionText?: q.questionText,
                            typeQuestion = updatedQuestion.typeQuestion?: q.typeQuestion,
                        )
                    } else {
                        q
                    }
                }
                _state.value = _state.value.copy(selectedQuestions = newList)
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

                val currentMap = _state.value.alternatives.toMutableMap()
                var found = false

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
                        found = true
                        break
                    }
                }

                if (found) {
                    _state.value = _state.value.copy(alternatives = currentMap)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al actualizar la alternativa: ${error.message}")
            }
        )
    }

    fun deleteAlternative(alternativeId: Int) {
        launchCatching(
            block = { repo.deleteAlternative(alternativeId) },
            onSuccess = {
                // Actualización OPTIMISTA local: remover del mapa sin recargar
                val currentMap = _state.value.alternatives.toMutableMap()
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
                    _state.value = _state.value.copy(alternatives = currentMap)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al eliminar la alternativa: ${error.message}")
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