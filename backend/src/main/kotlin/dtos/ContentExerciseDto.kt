package com.example.dtos

import kotlinx.serialization.Serializable
import models.TypeTextExercise

@Serializable
data class ContentExerciseDto(
    val id: Int,
    val nameExercise: String,
    val typeText: TypeTextExercise,
    val audioUrl: String? = null,
    val isActive: Boolean = false,
    val createdAt: String,
    val exerciseId: Int
)

@Serializable
data class CreateContentExerciseDto(
    val nameExercise: String,
    val typeText: TypeTextExercise,
    val audioUrl: String? = null,
    val isActive: Boolean? = false,
    val createdAt: String,
    val exerciseId: Int
)

@Serializable
data class UpdateContentExerciseDto(
    val nameExercise: String? = null,
    val typeText: TypeTextExercise? = null,
    val audioUrl: String? = null,
    val isActive: Boolean? = null,
    val exerciseId: Int? = null
)
