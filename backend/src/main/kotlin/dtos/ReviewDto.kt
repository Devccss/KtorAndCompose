package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UnitReviewStatusDto(
    val userId: Int,
    val unitId: Int,
    val testId: Int,
    val requiresReview: Boolean,
    val requiredExercises: Int,
    val reviewedExercises: Int,
    val remainingExercises: Int,
    val lastAttemptScore: Int? = null,
    val lastAttemptAt: String? = null
)

