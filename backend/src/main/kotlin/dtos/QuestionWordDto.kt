package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class QuestionWordDto(
    val id: Int,
    val questionId: Int,
    val wordId: Int
)

@Serializable
data class CreateQuestionWordDto(
    val questionId: Int,
    val wordId: Int
)

@Serializable
data class UpdateQuestionWordDto(
    val questionId: Int? = null,
    val wordId: Int? = null
)
