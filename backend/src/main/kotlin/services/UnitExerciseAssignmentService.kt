package com.example.services

import com.example.dtos.ExerciseDto
import com.example.dtos.UnitExerciseAssignmentDto
import io.ktor.server.plugins.BadRequestException
import models.AssignmentStatus
import models.AssignmentType
import models.TestExercises
import org.slf4j.LoggerFactory
import repositories.UnitExerciseAssignmentRecord
import repositories.UnitExerciseAssignmentRepository
import services.UnitService
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.random.Random

@Suppress("unused")
class UnitExerciseAssignmentService(
    private val assignmentRepository: UnitExerciseAssignmentRepository,
    private val exerciseService: ExerciseService,
    private val unitService: UnitService,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(UnitExerciseAssignmentService::class.java)

    private companion object {
        const val MAX_LIMIT = 5
        const val RECENT_ASSIGNMENT_WINDOW = 10
        const val ASSIGNMENT_TTL_SECONDS = 30L
    }

    fun getCurrentAssignment(unitId: Int, userId: Int, mode: String, limit: Int): UnitExerciseAssignmentDto {
        return resolveAssignment(unitId, userId, mode, limit, forceNew = false)
    }

    fun generateAssignment(unitId: Int, userId: Int, mode: String, limit: Int): UnitExerciseAssignmentDto {
        return resolveAssignment(unitId, userId, mode, limit, forceNew = true)
    }

    private fun resolveAssignment(unitId: Int, userId: Int, mode: String, limit: Int, forceNew: Boolean): UnitExerciseAssignmentDto {
        val assignmentType = parseMode(mode)
        val normalizedLimit = limit.coerceIn(1, MAX_LIMIT)

        userService.getUserById(userId) ?: throw BadRequestException("El usuario con ID $userId no existe.")
        unitService.getUnitById(unitId) ?: throw BadRequestException("La unidad con ID $unitId no existe.")

        if (forceNew) {
            assignmentRepository.expireActiveAssignments(userId, unitId, assignmentType)
        } else {
            assignmentRepository.getActiveAssignment(userId, unitId, assignmentType)?.let { current ->
                if (!isExpired(current) && !isCompleted(current, userId)) {
                    log.debug("Returning active assignment id={} unitId={} userId={} mode={}", current.id, unitId, userId, mode)
                    return toDto(current)
                }

                assignmentRepository.updateStatus(
                    current.id,
                    if (isCompleted(current, userId)) AssignmentStatus.USED else AssignmentStatus.EXPIRED
                )
            }
        }

        val exercises = selectExercises(unitId, userId, assignmentType, normalizedLimit)
        val created = assignmentRepository.createAssignment(
            userId = userId,
            unitId = unitId,
            assignmentType = assignmentType,
            exerciseIds = exercises.map { it.id }
        )

        return toDto(created)
    }

    private fun selectExercises(unitId: Int, userId: Int, assignmentType: AssignmentType, limit: Int): List<ExerciseDto> {
        val available = exerciseService.getByUnitId(unitId)
            .filter { it.isActive }
            .filterNot { isExerciseUsedInTests(it.id) }
            .distinctBy { it.id }

        if (available.isEmpty()) {
            throw BadRequestException("No hay ejercicios activos disponibles para la unidad $unitId.")
        }

        if (available.size <= limit) {
            return available.shuffled(Random.Default)
        }

        val completedIds = exerciseService.getExerciseCompletedByUser(userId)
            .map { it.exerciseId }
            .toSet()

        val recentIds = assignmentRepository.getRecentAssignments(userId, unitId, assignmentType, RECENT_ASSIGNMENT_WINDOW)
            .flatMap { it.exerciseIds }
            .distinct()

        val withoutRecent = available.filterNot { it.id in recentIds }
        val prioritizedPool = if (withoutRecent.size >= limit) withoutRecent else available

        val notCompleted = prioritizedPool.filterNot { it.id in completedIds }.shuffled(Random.Default)
        val completed = prioritizedPool.filter { it.id in completedIds }.shuffled(Random.Default)
        val selected = (notCompleted + completed).distinctBy { it.id }.take(limit)

        return if (selected.size < limit) {
            available.shuffled(Random.Default).take(limit)
        } else {
            selected
        }
    }

    private fun isExerciseUsedInTests(exerciseId: Int): Boolean {
        return transaction {
            TestExercises.selectAll().where { TestExercises.exerciseId eq exerciseId }.count() > 0
        }
    }

    private fun isExpired(record: UnitExerciseAssignmentRecord): Boolean {
        return java.time.Duration.between(record.createdAt, java.time.LocalDateTime.now()).seconds > ASSIGNMENT_TTL_SECONDS
    }

    private fun isCompleted(record: UnitExerciseAssignmentRecord, userId: Int): Boolean {
        val completedIds = exerciseService.getExerciseCompletedByUser(userId).map { it.exerciseId }.toSet()
        return record.exerciseIds.isNotEmpty() && record.exerciseIds.all { it in completedIds }
    }

    private fun parseMode(mode: String): AssignmentType {
        return runCatching { AssignmentType.valueOf(mode.trim().uppercase()) }
            .getOrElse { throw BadRequestException("Modo inválido: $mode. Usa 'initial' o 'review'.") }
    }

    private fun toDto(record: UnitExerciseAssignmentRecord): UnitExerciseAssignmentDto {
        val exercises = record.exerciseIds.mapNotNull { exerciseService.getById(it) }
        return UnitExerciseAssignmentDto(
            assignmentId = record.id,
            unitId = record.unitId,
            userId = record.userId,
            mode = record.assignmentType.name.lowercase(),
            exerciseIds = record.exerciseIds,
            exercises = exercises,
            persisted = true,
            status = record.status.name.lowercase(),
            createdAt = record.createdAt.toString()
        )
    }
}



