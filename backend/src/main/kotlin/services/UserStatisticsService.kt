package com.example.services

import com.example.dtos.UserStatsDto
import com.example.dtos.UnitDto
import services.UnitService

class UserStatisticsService(
	private val unitService: UnitService,
	private val exerciseService: ExerciseService,
	private val testService: TestService,
	private val userSessionLogsService: UserSessionLogsService,
	private val userService: UserService
) {

	fun getUserStats(userId: Int): UserStatsDto {
		val completedUnits: List<UnitDto> = unitService.getUnitsCompletedByUser(userId)
		val completedExercises = exerciseService.getExerciseCompletedByUser(userId)
		val completedTests = testService.getTestsCompletedByUser(userId)
		val failedTests = testService.getTestsFailedByUser(userId, minScore = 60)

		val totalSeconds = userSessionLogsService.getSessionsByUserId(userId)
			.sumOf { it.durationSeconds ?: 0L }
		val totalHours = totalSeconds.toDouble() / 3600.0

		val averageTestScore = if (completedTests.isNotEmpty()) completedTests.map { it.score }.average() else 0.0

		return UserStatsDto(
			userId = userId,
			completedUnitsCount = completedUnits.size,
			completedExercisesCount = completedExercises.size,
			completedTestsCount = completedTests.size,
			failedTestsCount = failedTests.size,
			reviewedUnitsCount = completedUnits.size,
			weeklyHours = totalHours,
			averageTestScore = averageTestScore
		)
	}

	fun getUserDetailedStats(userId: Int) = userService.getUserById(userId) // placeholder

}


