package org.example.project.dtos

import kotlinx.serialization.Serializable
@Serializable
data class QuestionDto(
    val id: Int,
    val exerciseContentId: Int,
    val questionText: String,
    val orderQuestion: Int,
    val isActive: Boolean? = false,
    val createdAt: String
)
@Serializable
data class CreateQuestionDto(
    val questionText: String,
    val orderQuestion: Int? = null,
    val isActive: Boolean? = false,
)

@Serializable
data class UpdateQuestionDto(
    val exerciseContentId: Int? = null,
    val questionText: String? = null,
    val orderQuestion: Int? = null,
    val isActive: Boolean? = null,
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
@Serializable
data class FilterQuestionsDto(
    val exerciseContentId: Int? = null,
    val questionText: String? = null,
    val isActive: Boolean? = null
)