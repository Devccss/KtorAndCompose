package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDto(
    val id: Int,
    val unitId: Int,
    val name: String,
    val description: String? = null,
    val orderExercise: Int,
    val isActive: Boolean = false,
    val createdAt: String
)

@Serializable
data class CreateExerciseDto(
    val unitId: Int,
    val name: String,
    val description: String? = null,
    val orderExercise: Int? = null,
    val isActive: Boolean? = false,
)

@Serializable
data class UpdateExerciseDto(
    val unitId: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val orderExercise: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class FilterExercisesDto(
    val name: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class ExerciseCompletedDto(
    val id: Int,
    val userId: Int,
    val exerciseId: Int,
    val completedAt: String
)
@Serializable
data class CreateExerciseCompletedDto(
    val userId: Int,
    val exerciseId: Int,
)

@Serializable
data class UpdateExerciseCompletedDto(
    val userId: Int? = null,
    val exerciseId: Int? = null,
    val completedAt: String? = null
)
