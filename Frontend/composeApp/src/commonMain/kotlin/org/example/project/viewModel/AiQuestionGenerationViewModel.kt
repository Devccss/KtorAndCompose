package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.dtos.AiGeneratedQuestionDto
import org.example.project.dtos.GenerateQuestionsFromAiResponseDto
import org.example.project.service.AiQuestionGenerationService

data class AiQuestionGenerationUiState(
    val questionCountInput: String = "1",
    val language: String = "es",
    val difficulty: String = "",
    val lastResponse: GenerateQuestionsFromAiResponseDto? = null,
    val generatedQuestions: List<AiGeneratedQuestionDto> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false
)

class AiQuestionGenerationViewModel(
    private val service: AiQuestionGenerationService
) : ViewModel(), ScreenModel {

    private val _state = MutableStateFlow(AiQuestionGenerationUiState())
    val state: StateFlow<AiQuestionGenerationUiState> = _state

    fun updateQuestionCount(value: String) {
        _state.update { it.copy(questionCountInput = value.filter { ch -> ch.isDigit() }.takeIf { it.isNotBlank() } ?: "1") }
    }

    fun updateLanguage(value: String) {
        _state.update { it.copy(language = value) }
    }

    fun updateDifficulty(value: String) {
        _state.update { it.copy(difficulty = value) }
    }

    fun clearGeneratedQuestions() {
        _state.update {
            it.copy(
                lastResponse = null,
                generatedQuestions = emptyList(),
                error = null
            )
        }
    }

    fun generateQuestions(contentId: Int) {
        val questionCount = _state.value.questionCountInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val language = _state.value.language.trim().ifBlank { "es" }
        val difficulty = _state.value.difficulty.trim().takeIf { it.isNotBlank() }

        launchCatching(
            block = {
                service.generateQuestions(
                    contentId = contentId,
                    questionCount = questionCount,
                    language = language,
                    difficulty = difficulty
                )
            },
            onSuccess = { response ->
                _state.update {
                    it.copy(
                        lastResponse = response,
                        generatedQuestions = response.suggestedQuestions,
                        error = null
                    )
                }
            },
            onError = { error ->
                _state.update {
                    it.copy(
                        error = "Error al generar preguntas con IA: ${error.message}",
                        generatedQuestions = emptyList(),
                        lastResponse = null
                    )
                }
            }
        )
    }

    fun confirmQuestion(
        contentId: Int,
        question: AiGeneratedQuestionDto,
        onSuccess: () -> Unit = {}
    ) {
        launchCatching(
            block = {
                service.confirmQuestion(
                    contentId = contentId,
                    questionText = question.questionText,
                    alternatives = question.alternatives,
                    isActive = true
                )
            },
            onSuccess = {
                _state.update { currentState ->
                    currentState.copy(
                        generatedQuestions = currentState.generatedQuestions - question,
                        error = null
                    )
                }
                onSuccess()
            },
            onError = { error ->
                _state.update { it.copy(error = "Error al confirmar la pregunta: ${error.message}") }
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
        } catch (e: Exception) {
            onError(e)
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }
}

