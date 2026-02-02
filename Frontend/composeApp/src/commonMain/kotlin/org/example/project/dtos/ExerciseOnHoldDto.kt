package org.example.project.dtos

import kotlinx.serialization.Serializable


@Serializable

data class ExerciseOnHoldDto(
    val id: Int,
    val exerciseId: Int,
    val userId: Int,
    val failureDate: String
)

data class CreateExerciseOnHoldDto(
    val exerciseId: Int,
    val userId: Int,
    val failureDate: String
)

data class UpdateExerciseOnHoldDto(
    val exerciseId: Int? = null,
    val userId: Int? = null,
    val failureDate: String? = null
)