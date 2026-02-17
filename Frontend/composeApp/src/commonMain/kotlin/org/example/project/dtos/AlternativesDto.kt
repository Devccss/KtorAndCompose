package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AlternativesDto(
    var id: Int,
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean? = false,
    val createdAt: String
)

@Serializable
data class CreateAlternativeDto(
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean? = false
)

@Serializable
data class UpdateAlternativeDto(
    val text: String? = null,
    val isCorrect: Boolean? = null
)