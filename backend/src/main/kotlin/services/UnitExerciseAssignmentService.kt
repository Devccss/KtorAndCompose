package com.example.services

// ...existing code...
import com.example.dtos.UnitExerciseAssignmentDto
import com.example.dtos.UnitRetakeStatusDto
import io.ktor.server.plugins.BadRequestException
// ...existing code...
import models.AssignmentType
import models.TestExercises
import org.slf4j.LoggerFactory
import repositories.UnitExerciseAssignmentRecord
import repositories.UnitExerciseAssignmentRepository
import services.UnitService
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Duration
import java.time.LocalDateTime

@Suppress("unused")
class UnitExerciseAssignmentService(
    private val assignmentRepository: UnitExerciseAssignmentRepository,
    private val exerciseService: ExerciseService,
    private val unitService: UnitService,
    private val userService: UserService,
    private val testService: TestService
) {

    private val log = LoggerFactory.getLogger(UnitExerciseAssignmentService::class.java)

    private companion object {
        const val MAX_LIMIT = 5
        const val RECENT_ASSIGNMENT_WINDOW = 10
        const val ASSIGNMENT_TTL_SECONDS = 30 * 60L // 8 minutes
        const val PASSING_SCORE = 70
    }



    fun getActiveAssignment(
        unitId: Int,
        userId: Int
    ): UnitExerciseAssignmentDto? {

        val assignment =
            assignmentRepository.getActiveAssignmentAnyType(
                userId = userId,
                unitId = unitId
            ) ?: return null

        return toDto(assignment)
    }
    fun shouldRetakeUnit(unitId: Int, userId: Int, assignedExerciseIds: List<Int>): UnitRetakeStatusDto {
        unitService.getUnitById(unitId) ?: throw BadRequestException("La unidad con ID $unitId no existe.")
        userService.getUserById(userId) ?: throw BadRequestException("El usuario con ID $userId no existe.")

        val test = testService.getTestsByUnitId(unitId)
        if (test == null) {
            log.info("shouldRetakeUnit: no test found for unit=$unitId -> shouldRetake=false")
            return UnitRetakeStatusDto(
                shouldRetake = false,
                reason = null,
                lastTestScore = null,
                lastTestDate = null,
                expirationDate = null,
                minutesRemaining = null
            )
        }

        val reviewStatus = testService.getUnitReviewStatus(
            userId, unitId, test.id,
            assignedExerciseIds = assignedExerciseIds
        )
        log.info("shouldRetakeUnit: reviewStatus for user=$userId unit=$unitId test=${test.id} -> requiresReview=${reviewStatus.requiresReview}, score=${reviewStatus.lastAttemptScore}")
        
        // Solo retoma si falló el test (score < PASSING_SCORE)
        // requiresReview es para ejercicios pendientes de revisar, NO es motivo para retomar
         val score = reviewStatus.lastAttemptScore ?: run {
            log.info("shouldRetakeUnit: no test score found for user=$userId unit=$unitId -> shouldRetake=true reason=no_test_taken")
            return UnitRetakeStatusDto(
                shouldRetake = true,
                reason = "no_test_taken",
                lastTestScore = null,
                lastTestDate = null,
                expirationDate = null,
                minutesRemaining = null
            )
        }
        
        // Solo retoma si falló (score < 70), NO por requiresReview
        if (score < PASSING_SCORE) {
            log.info("shouldRetakeUnit: user=$userId unit=$unitId must retake (reason=low_score, score=$score < $PASSING_SCORE)")
            return UnitRetakeStatusDto(
                shouldRetake = true,
                reason = "low_score",
                lastTestScore = score,
                lastTestDate = reviewStatus.lastAttemptAt,
                expirationDate = null,
                minutesRemaining = null
            )
        }
        
        // Usuario pasó el test con score suficiente: no retoma, aunque requiresReview sea true
        log.info("shouldRetakeUnit: user=$userId unit=$unitId does NOT need retake (score=$score >= $PASSING_SCORE, requiresReview=${reviewStatus.requiresReview})")
         return UnitRetakeStatusDto(
             shouldRetake = false,
             reason = null,
             lastTestScore = score,
             lastTestDate = reviewStatus.lastAttemptAt,
             expirationDate = null,
             minutesRemaining = null
         )
     }


    private fun isExerciseUsedInTests(exerciseId: Int): Boolean {
        return transaction {
            TestExercises.selectAll().where { TestExercises.exerciseId eq exerciseId }.count() > 0
        }
    }

    private fun isExpired(record: UnitExerciseAssignmentRecord): Boolean {
        return Duration.between(record.createdAt, LocalDateTime.now()).seconds > ASSIGNMENT_TTL_SECONDS
    }

    private fun parseMode(mode: String): AssignmentType {
        return runCatching { AssignmentType.valueOf(mode.trim().uppercase()) }
            .getOrElse { throw BadRequestException("Modo inválido: $mode. Usa 'initial' o 'review'.") }
    }

    private fun buildRetakeKey(unitId: Int, userId: Int, assignedExerciseIds: List<Int> ): String? {
        val test = testService.getTestsByUnitId(unitId) ?: return null
        val reviewStatus = testService.getUnitReviewStatus(userId, unitId, test.id, assignedExerciseIds)
        val score = reviewStatus.lastAttemptScore ?: return null

        if (!reviewStatus.requiresReview && score >= PASSING_SCORE) return null

        // Marca estable del episodio de retake: cambia cuando cambia el último intento del test o el score
        return listOf(
            test.id,
            userId,
            unitId,
            reviewStatus.lastAttemptAt ?: "no-attempt",
            score,
            reviewStatus.requiresReview
        ).joinToString("|")
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

    fun getCurrentAssignment(unitId: Int, userId: Int, mode: String, limit: Int, assignedExerciseIds: List<Int>): UnitExerciseAssignmentDto? {
        val assignmentType = parseMode(mode)
        log.info("getCurrentAssignment called: unitId=$unitId, userId=$userId, mode=$mode, limit=$limit, assignmentType=$assignmentType")
        val active = assignmentRepository.getActiveAssignment(userId, unitId, assignmentType)
        val currentRetakeKey = buildRetakeKey(unitId, userId, assignedExerciseIds )
        log.info("getCurrentAssignment: currentRetakeKey=$currentRetakeKey")

        if (active == null) {
            log.info("getCurrentAssignment: no active assignment found for user=$userId unit=$unitId mode=$assignmentType")
            return null
        }

        log.info("getCurrentAssignment: found active assignment id=${active.id}, createdAt=${active.createdAt}, status=${active.status}")

        // If expired, expire and return null
        val now = LocalDateTime.now()
        val secondsSinceCreation = Duration.between(active.createdAt, now).seconds
        if (isExpired(active)) {
            log.info("getCurrentAssignment: active assignment id=${active.id} is EXPIRED (created ${secondsSinceCreation}s ago, TTL=${ASSIGNMENT_TTL_SECONDS}s) -> expiring")
            assignmentRepository.expireActiveAssignments(userId, unitId, assignmentType)
            return null
        }
        log.info("getCurrentAssignment: assignment id=${active.id} is NOT expired (created ${secondsSinceCreation}s ago, TTL=${ASSIGNMENT_TTL_SECONDS}s)")

        // Si el usuario debe retomar, pero ya existe una asignación para el mismo retakeKey, se reutiliza
        val retake = runCatching { shouldRetakeUnit(unitId, userId , assignedExerciseIds) }.getOrNull()
        log.info("getCurrentAssignment: retake check for user=$userId unit=$unitId -> shouldRetake=${retake?.shouldRetake}, reason=${retake?.reason}")
        if (retake?.shouldRetake == true) {
            if (currentRetakeKey != null && active.retakeKey == currentRetakeKey) {
                log.info("getCurrentAssignment: user=$userId still on same retakeKey=${active.retakeKey} -> reusing active assignment id=${active.id}")
                val dto = toDto(active)
                log.info("getCurrentAssignment: returning assignment id=${dto.assignmentId} exercises=${dto.exerciseIds}")
                return dto
            }

            log.info("getCurrentAssignment: user=$userId must retake unit=$unitId (reason=${retake.reason}) and retakeKey changed from ${active.retakeKey} to $currentRetakeKey -> expiring active assignment id=${active.id}")
            assignmentRepository.expireActiveAssignments(userId, unitId, assignmentType)
            return null
        }
        log.info("getCurrentAssignment: user=$userId does NOT need to retake unit=$unitId -> returning assignment")

        val dto = toDto(active)
        log.info("getCurrentAssignment: returning assignment id=${dto.assignmentId} exercises=${dto.exerciseIds}")
        return dto
    }

    fun generateAssignment(unitId: Int, userId: Int, mode: String, limitParam: Int, assignedExerciseIds: List<Int>): UnitExerciseAssignmentDto {
        log.info("generateAssignment called: unitId=$unitId, userId=$userId, mode=$mode, limitParam=$limitParam")
        unitService.getUnitById(unitId) ?: throw BadRequestException("La unidad con ID $unitId no existe.")
        userService.getUserById(userId) ?: throw BadRequestException("El usuario con ID $userId no existe.")

        val limit = limitParam.coerceAtMost(MAX_LIMIT).coerceAtLeast(1)
        val currentRetakeKey = buildRetakeKey(unitId, userId, assignedExerciseIds)

        // Decide si usar modo REVIEW u INITIAL consultando el servicio de tests
        val test = testService.getTestsByUnitId(unitId)
        val assignmentType = if (test != null) {
            val reviewStatus = testService.getUnitReviewStatus(userId, unitId, test.id, assignedExerciseIds)
            log.info("generateAssignment: test found for unit=$unitId id=${test.id}, reviewStatus=$reviewStatus")
            if (reviewStatus.requiresReview) AssignmentType.REVIEW else parseMode(mode)
        } else {
            parseMode(mode)
        }
        log.info("generateAssignment: resolved assignmentType=$assignmentType")

        // Si existe una asignación activa y no ha expirado, comprobar si debe renovarse por retake
        val active = assignmentRepository.getActiveAssignment(userId, unitId, assignmentType)
        if (active != null) {
            log.info("generateAssignment: found active assignment id=${active.id}, createdAt=${active.createdAt}")
            if (isExpired(active)) {
                log.info("generateAssignment: active assignment id=${active.id} is expired -> expiring")
                assignmentRepository.expireActiveAssignments(userId, unitId, assignmentType)
            } else {
                val retake = runCatching { shouldRetakeUnit(unitId, userId, assignedExerciseIds) }.getOrNull()
                log.info("generateAssignment: retake check -> shouldRetake=${retake?.shouldRetake}, reason=${retake?.reason}, currentRetakeKey=$currentRetakeKey, storedRetakeKey=${active.retakeKey}")
                if (retake?.shouldRetake == true && currentRetakeKey != null && active.retakeKey == currentRetakeKey) {
                    val dto = toDto(active)
                    log.info("generateAssignment: reusing active assignment for same retakeKey id=${dto.assignmentId} exercises=${dto.exerciseIds}")
                    return dto
                }
                if (retake?.shouldRetake == true) {
                    log.info("generateAssignment: user=$userId must retake unit=$unitId and retakeKey changed -> expiring active assignment id=${active.id}")
                    assignmentRepository.expireActiveAssignments(userId, unitId, assignmentType)
                } else {
                    val dto = toDto(active)
                    log.info("generateAssignment: reusing active assignment id=${dto.assignmentId} exercises=${dto.exerciseIds}")
                    return dto
                }
            }
        }

        // Seleccionar ejercicios candidatos
        val allExercises = exerciseService.getByUnitId(unitId).filter { it.isActive }
        if (allExercises.isEmpty()) throw BadRequestException("No hay ejercicios activos en la unidad $unitId")

        val recent = assignmentRepository.getRecentAssignments(userId, unitId, assignmentType, RECENT_ASSIGNMENT_WINDOW)
            .flatMap { it.exerciseIds }
            .toSet()

        // Filtrar ejercicios que NO están en tests
        val notInTests = allExercises.map { it.id }.filter { !isExerciseUsedInTests(it) }
        
        // Prioridad 1: ejercicios no recientes
        val notRecent = notInTests.filter { !recent.contains(it) }
        
        // Seleccionar estrategia: 
        // - Si hay suficientes no-recientes, usarlos
        // - Si no, llenar con recientes hasta alcanzar limit
        val chosen = if (notRecent.size >= limit) {
            log.info("generateAssignment: selecting from ${notRecent.size} non-recent exercises (limit=$limit)")
            notRecent.shuffled().take(limit)
        } else {
            val firstBatch = notRecent.shuffled()
            val remaining = limit - firstBatch.size
            val secondBatch = (notInTests - firstBatch.toSet()).shuffled().take(remaining)
            log.info("generateAssignment: selected ${firstBatch.size} non-recent + ${secondBatch.size} recent (limit=$limit)")
            (firstBatch + secondBatch).shuffled()
        }

        if (chosen.isEmpty()) throw BadRequestException("No se pudieron seleccionar ejercicios para la asignación")

        log.info("generateAssignment: chosen exercises for user=$userId unit=$unitId (total=${chosen.size}, limit=$limit) -> $chosen")

        val created = assignmentRepository.createAssignment(userId, unitId, assignmentType, chosen, currentRetakeKey)
        log.info("generateAssignment: created assignment id=${created.id} exercises=${created.exerciseIds}")

        // Expirar otras asignaciones activas del mismo tipo por seguridad (ya lo hicimos si había una expiración)
        // pero aseguramos que el nuevo sea el único activo
        // La función createAssignment inserta con status ACTIVE por defecto

        return toDto(created)
    }
}



