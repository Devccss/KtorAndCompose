package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class GenerateQuestionsFromAiRequestDto(
    val questionCount: Int = 1,
    val language: String = "es",
    val difficulty: String? = null
)

@Serializable
data class GenerateQuestionsFromAiResponseDto(
    val contentId: Int,
    val accepted: Int,
    val rejected: List<RejectedAiQuestionDto>,
    val suggestedQuestions: List<AiGeneratedQuestionDto>
)

@Serializable
data class RejectedAiQuestionDto(
    val index: Int,
    val reason: String
)

@Serializable
data class ConfirmAiQuestionRequestDto(
    val questionText: String,
    val alternatives: List<AiGeneratedAlternativeDto>,
    val isActive: Boolean = true
)

@Serializable
data class ConfirmAiQuestionResponseDto(
    val question: QuestionDto,
    val alternatives: List<AlternativeDto>
)


// Payload que Ktor envia a Python
@Serializable
data class AiGenerationRequestDto(
    val schemaVersion: String = "1.0",
    val contentId: Int,
    val exerciseId: Int,
    val textContent: String,
    val grammarExplanation: String,
    val questionCount: Int,
    val language: String,
    val difficulty: String?
)

// Respuesta que Python devuelve a Ktor
@Serializable
data class AiGenerationResponseDto(
    val questions: List<AiGeneratedQuestionDto>
)

@Serializable
data class AiGeneratedQuestionDto(
    val questionText: String,
    val alternatives: List<AiGeneratedAlternativeDto>
)

@Serializable
data class AiGeneratedAlternativeDto(
    val text: String,
    val isCorrect: Boolean
)
