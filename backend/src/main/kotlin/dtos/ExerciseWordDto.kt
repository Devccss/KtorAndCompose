package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseWordDto(
    val id: Int,
    val exerciseId : Int,
    val wordId: Int
)

@Serializable
data class CreateExerciseWordDto(
    val exerciseId : Int,
    val wordId: Int
)

@Serializable
data class UpdateExerciseWordDto(
    val exerciseId : Int? = null,
    val wordId: Int? = null
)
