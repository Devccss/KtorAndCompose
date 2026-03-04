package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AlternativeDto(
    val id: Int,
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean? = false,
    val createdAt: String
)

@Serializable
data class CreateAlternativeDto(
    val text: String,
    val isCorrect: Boolean? = false
)

@Serializable
data class UpdateAlternativeDto(
    val text: String? = null,
    val isCorrect: Boolean? = null
)