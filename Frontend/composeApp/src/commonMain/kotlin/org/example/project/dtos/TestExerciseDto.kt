package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class TestExerciseDto(
    val id: Int,
    val testId: Int,
    val exerciseId: Int
)
@Serializable
data class CreateTestExerciseDto(
    val testId: Int,
    val exerciseId: Int
)
@Serializable
data class UpdateTestExerciseDto(
    val testId: Int? = null,
    val exerciseId: Int? = null
)
