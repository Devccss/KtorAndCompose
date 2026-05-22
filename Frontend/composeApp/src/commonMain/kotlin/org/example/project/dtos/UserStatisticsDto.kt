package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UserStatsDto(
    val userId: Int? = null,
    val completedUnits: Int? = null,
    val completedExercises: Int? = null,
    val completedTests: Int? = null,
    val failedTests: Int? = null,
    val weeklyHours: Double? = null,
    val sessionCount: Int? = null
)

@Serializable
data class UserWeeklyHoursDto(
    val userId: Int,
    val weeklyHours: Double,
    val sessionCount: Int
)
