package repositories

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.BadRequestException
import models.AssignmentStatus
import models.AssignmentType
import models.UnitExerciseAssignments
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

data class UnitExerciseAssignmentRecord(
    val id: Int,
    val userId: Int,
    val unitId: Int,
    val assignmentType: AssignmentType,
    val retakeKey: String?,
    val exerciseIds: List<Int>,
    val status: AssignmentStatus,
    val createdAt: LocalDateTime
)

class UnitExerciseAssignmentRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private fun rowToRecord(row: ResultRow): UnitExerciseAssignmentRecord {
        val exerciseIds = runCatching {
            json.decodeFromString(ListSerializer(Int.serializer()), row[UnitExerciseAssignments.exerciseIds])
        }.getOrElse { emptyList() }

        return UnitExerciseAssignmentRecord(
            id = row[UnitExerciseAssignments.id].value,
            userId = row[UnitExerciseAssignments.userId],
            unitId = row[UnitExerciseAssignments.unitId],
            assignmentType = row[UnitExerciseAssignments.assignmentType],
            retakeKey = row[UnitExerciseAssignments.retakeKey],
            exerciseIds = exerciseIds,
            status = row[UnitExerciseAssignments.status],
            createdAt = row[UnitExerciseAssignments.createdAt]
        )
    }

    fun getActiveAssignment(userId: Int, unitId: Int, assignmentType: AssignmentType): UnitExerciseAssignmentRecord? = transaction {
        UnitExerciseAssignments.selectAll()
            .where {
                (UnitExerciseAssignments.userId eq userId) and
                        (UnitExerciseAssignments.unitId eq unitId) and
                        (UnitExerciseAssignments.assignmentType eq assignmentType) and
                        (UnitExerciseAssignments.status eq AssignmentStatus.ACTIVE)
            }
            .map(::rowToRecord).maxByOrNull { it.createdAt }
    }

    fun getRecentAssignments(userId: Int, unitId: Int, assignmentType: AssignmentType, limit: Int = 10): List<UnitExerciseAssignmentRecord> = transaction {
        UnitExerciseAssignments.selectAll()
            .where {
                (UnitExerciseAssignments.userId eq userId) and
                    (UnitExerciseAssignments.unitId eq unitId) and
                    (UnitExerciseAssignments.assignmentType eq assignmentType)
            }
            .map(::rowToRecord)
            .sortedByDescending { it.createdAt }
            .take(limit)
    }

    fun createAssignment(
        userId: Int,
        unitId: Int,
        assignmentType: AssignmentType,
        exerciseIds: List<Int>,
        retakeKey: String? = null,
        status: AssignmentStatus = AssignmentStatus.ACTIVE
    ): UnitExerciseAssignmentRecord = transaction {
        if (exerciseIds.isEmpty()) throw BadRequestException("No se puede crear una asignación sin ejercicios.")

        val newId = UnitExerciseAssignments.insert {
            it[this.userId] = userId
            it[this.unitId] = unitId
            it[this.assignmentType] = assignmentType
            it[this.retakeKey] = retakeKey
            it[this.exerciseIds] = json.encodeToString(ListSerializer(Int.serializer()), exerciseIds)
            it[this.status] = status
            it[this.createdAt] = LocalDateTime.now()
        }[UnitExerciseAssignments.id]

        getById(newId.value) ?: throw BadRequestException("No se pudo crear la asignación de ejercicios.")
    }

    fun getById(id: Int): UnitExerciseAssignmentRecord? = transaction {
        UnitExerciseAssignments.selectAll()
            .where { UnitExerciseAssignments.id eq id }
            .singleOrNull()
            ?.let(::rowToRecord)
    }

    fun updateStatus(id: Int, status: AssignmentStatus) {
        transaction {
            UnitExerciseAssignments.update({ UnitExerciseAssignments.id eq id }) {
                it[this.status] = status
            }
        }
    }

    fun expireActiveAssignments(userId: Int, unitId: Int, assignmentType: AssignmentType) {
        transaction {
            UnitExerciseAssignments.update({
                (UnitExerciseAssignments.userId eq userId) and
                    (UnitExerciseAssignments.unitId eq unitId) and
                    (UnitExerciseAssignments.assignmentType eq assignmentType) and
                    (UnitExerciseAssignments.status eq AssignmentStatus.ACTIVE)
            }) {
                it[status] = AssignmentStatus.EXPIRED
            }
        }
    }
}


