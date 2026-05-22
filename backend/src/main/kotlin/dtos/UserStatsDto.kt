package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UserStatsDto(
    val userId: Int,
    val completedUnitsCount: Int,
    val completedExercisesCount: Int,
    val completedTestsCount: Int,
    val failedTestsCount: Int,
    val reviewedUnitsCount: Int,
    val weeklyHours: Double,
    val averageTestScore: Double
)

@Serializable
data class UserDetailedStatsDto(
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val completedUnits: List<UnitDto>,
    val completedExercises: List<Int>, // IDs de ejercicios completados
    val completedTests: List<TestCompletedDto>,
    val failedTests: List<TestCompletedDto>,
    val stats: UserStatsDto
)

