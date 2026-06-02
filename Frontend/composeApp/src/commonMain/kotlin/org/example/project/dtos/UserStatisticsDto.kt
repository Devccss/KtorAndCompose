package org.example.project.dtos
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
data class GeneralStatsDto(
    val totalUsers: Int,
    val totalUnits: Int,
    val totalExercises: Int,
    val totalTests: Int,
    val totalFailedTests: Int,
    val totalStudyHours: Double,
    val averageTestScore: Double
)

@Serializable
data class StudentStatsDto(
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val completedUnitsCount: Int,
    val completedExercisesCount: Int,
    val completedTestsCount: Int,
    val failedTestsCount: Int,
    val totalStudyHours: Double,
    val averageTestScore: Double
)

@Serializable
data class StudentsStatsSummaryDto(
    val totalStudents: Int,
    val completedUnitsCount: Int,
    val completedExercisesCount: Int,
    val completedTestsCount: Int,
    val failedTestsCount: Int,
    val totalStudyHours: Double,
    val averageTestScore: Double
)

@Serializable
data class StudentsStatsDto(
    val summary: StudentsStatsSummaryDto,
    val students: List<StudentStatsDto>
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



@Serializable
data class UserWeeklyHoursDto(
    val userId: Int,
    val weeklyHours: Double,
    val sessionCount: Int
)
