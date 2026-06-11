package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LearningDashboardDto(
    val totalExercises: Int,
    val completedExercises: Int,
    val totalUnits: Int,
    val completedUnits: Int,
    val currentUnitId: Int?,
    val units: List<UnitProgressDto>
)
@Serializable
data class UnitProgressDto(
    val unitId: Int,
    val unitName: String,
    val orderUnit: Int,

    val totalExercises: Int,
    val completedExercises: Int,

    val progressPercentage: Int,

    val unlocked: Boolean,
    val completed: Boolean,

    val testId: Int?,
    val testName: String?,

    val testLocked: Boolean,

    val requiresReview: Boolean,
    val remainingReviewExercises: Int
)