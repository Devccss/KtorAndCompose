package com.example.dtos

import kotlinx.serialization.Serializable
import models.TypeQuestion

@Serializable
data class QuestionDto(
    val id: Int,
    val questionText: String,
    val typeQuestion: TypeQuestion,
    val contentId: Int? = null,
    val createdAt: String
)

@Serializable
data class CreateQuestionDto(
    val questionText: String,
    val typeQuestion: TypeQuestion? = TypeQuestion.OPEN,
    val contentId: Int? = null,
    val createdAt: String
)

@Serializable
data class UpdateQuestionDto(
    val questionText: String? = null,
    val typeQuestion: TypeQuestion? = null,
    val contentId: Int? = null
)

@Serializable
data class QuestionCompletedDto(
    val id: Int,
    val userId: Int,
    val questionId: Int,
    val answeredAt: String
)
@Serializable
data class CreateQuestionCompletedDto(
    val userId: Int,
    val questionId: Int,
    val answeredAt: String
)
@Serializable
data class UpdateQuestionCompletedDto(
    val userId: Int? = null,
    val questionId: Int? = null,
    val answeredAt: String? = null
)