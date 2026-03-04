package org.example.project.dtos

import kotlinx.serialization.Serializable

enum class TypeTextExercise { NORMAL, BOLD, ITALIC, UNDERLINE }
enum class TypeQuestion { ALTERNATIVE, OPEN }
@Serializable
data class QuestionDto(
    val id: Int,
    val textContent: String,
    val typeText: TypeTextExercise,
    val grammarExplanation: String,
    val audioUrl: String? = null,
    val isActive: Boolean? = false,
    val exerciseId: Int,
    val questionText: String,
    val typeQuestion: TypeQuestion,
    val orderQuestion: Int,
    val createdAt: String
)

@Serializable
data class CreateQuestionDto(
    val textContent: String,
    val typeText: TypeTextExercise,
    val grammarExplanation: String,
    val audioUrl: String?,
    val isActive: Boolean? = false,
    val questionText: String,
    val typeQuestion: TypeQuestion,
    val orderQuestion: Int? = null,
)

@Serializable
data class UpdateQuestionDto(
    val textContent: String? = null,
    val typeText: TypeTextExercise? = null,
    val grammarExplanation: String? = null,
    val audioUrl: String? = null,
    val isActive: Boolean? = false,
    val exerciseId: Int? = null,
    val questionText: String? = null,
    val typeQuestion: TypeQuestion? = null,
    val orderQuestion: Int? = null,
)

@Serializable
data class CreateQuestionCompletedDto(
    val userId: Int,
    val questionId: Int,
    val completedAt: String
)

@Serializable
data class QuestionCompletedDto(
    val id: Int,
    val userId: Int,
    val questionId: Int,
    val completedAt: String
)

@Serializable
data class UpdateQuestionCompletedDto(
    val userId: Int? = null,
    val questionId: Int? = null,
    val completedAt: String? = null
)
