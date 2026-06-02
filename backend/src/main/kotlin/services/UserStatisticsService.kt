package com.example.services

import com.example.dtos.GeneralStatsDto
import com.example.dtos.StudentsStatsSummaryDto
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

	fun getAllUsersStats(): GeneralStatsDto {
		val allUsers = userService.getAllUsers()
		val allCompletedUnits = unitService.getAllUnitsCompleted()
		val allCompletedExercises = exerciseService.getAllExerciseCompleted()
		val allCompletedTests = testService.getAllTestCompleted()
		val allFailedTests = testService.getAllTestsFailedByStudents(minScore = 60)
		val allSessions = userSessionLogsService.getAllSessions()

		val totalHours = allSessions.sumOf { it.durationSeconds ?: 0L } / 3600.0
		val averageTestScore = if (allCompletedTests.isNotEmpty()) allCompletedTests.map { it.score }.average() else 0.0

		return GeneralStatsDto(
			totalUsers = allUsers.size,
			totalUnits = allCompletedUnits.size,
			totalExercises = allCompletedExercises.size,
			totalTests = allCompletedTests.size,
			totalFailedTests = allFailedTests.size,
			totalStudyHours = totalHours,
			averageTestScore = averageTestScore
		)

	}
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

	fun getAllStudentsStats(): StudentsStatsSummaryDto {
		val students = userService.getAllStudents()
		if (students.isEmpty()) {
			return StudentsStatsSummaryDto(
				totalStudents = 0,
				completedUnitsCount = 0,
				completedExercisesCount = 0,
				completedTestsCount = 0,
				failedTestsCount = 0,
				totalStudyHours = 0.0,
				averageTestScore = 0.0
			)
		}

		val completedUnits = unitService.getAllUnitsCompletedByStudents()
		val completedExercises = exerciseService.getAllExercisesCompletedByStudents()
		val completedTests = testService.getAllTestsCompletedByStudents()
		val failedTests = testService.getAllTestsFailedByStudents(minScore = 60)
		val sessions = userSessionLogsService.getAllSessionsByStudents()
		val totalCompletedTests = completedTests.size
		val totalTestScore = completedTests.sumOf { it.score }
		val totalStudySeconds = sessions.sumOf { it.durationSeconds ?: 0L }

		return StudentsStatsSummaryDto(
			totalStudents = students.size,
			completedUnitsCount = completedUnits.size,
			completedExercisesCount = completedExercises.size,
			completedTestsCount = totalCompletedTests,
			failedTestsCount = failedTests.size,
			totalStudyHours = totalStudySeconds.toDouble() / 3600.0,
			averageTestScore = if (totalCompletedTests > 0) {
				totalTestScore.toDouble() / totalCompletedTests.toDouble()
			} else {
				0.0
			}
		)
	}

	fun getUserDetailedStats(userId: Int) = userService.getUserById(userId) // placeholder

}


