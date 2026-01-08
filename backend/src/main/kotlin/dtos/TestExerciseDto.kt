package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable

data class TestExerciseDto(
    val id: Int,
    val testId: Int,
    val exerciseId: Int
)

data class CreateTestExerciseDto(
    val testId: Int,
    val exerciseId: Int
)

data class UpdateTestExerciseDto(
    val testId: Int? = null,
    val exerciseId: Int? = null
)

