package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UnitExerciseAssignmentDto(
    val assignmentId: Int? = null,
    val unitId: Int,
    val userId: Int,
    val mode: String,
    val exerciseIds: List<Int>,
    val exercises: List<ExerciseDto>,
    val persisted: Boolean = true,
    val status: String? = null,
    val createdAt: String? = null
)

