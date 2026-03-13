package org.example.project.dtos

import kotlinx.serialization.Serializable
enum class ContentType { READING, LISTENING }
@Serializable
data class ExerciseContentDto(
    val id: Int,
    val exerciseId : Int,
    val contentType: ContentType,
    val textContent: String,
    val grammarExplanation: String,
    val audioUrl: String? = null,
    val createdAt: String
)

@Serializable
data class CreateExerciseContentDto(
    val contentType: ContentType,
    val textContent: String,
    val grammarExplanation: String,
    val audioUrl: String? = null
)

@Serializable
data class UpdateExerciseContentDto(
    val exerciseId : Int? = null,
    val contentType: ContentType? = null,
    val textContent: String? = null,
    val grammarExplanation: String? = null,
    val audioUrl: String? = null
)