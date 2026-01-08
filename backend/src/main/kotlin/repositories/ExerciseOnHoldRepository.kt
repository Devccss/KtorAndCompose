package repositories

import com.example.dtos.CreateExerciseOnHoldDto
import com.example.dtos.ExerciseOnHoldDto
import com.example.dtos.UpdateExerciseOnHoldDto
import io.ktor.server.plugins.BadRequestException
import models.ExercisesOnHold

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class ExerciseOnHoldRepository {

    private fun resultRowTo(row: ResultRow): ExerciseOnHoldDto {
        return ExerciseOnHoldDto(
            id = row[ExercisesOnHold.id].value,
            exerciseId = row[ExercisesOnHold.exerciseId],
            userId = row[ExercisesOnHold.userId],
            failureDate = row[ExercisesOnHold.failureDate].toString()
        )
    }

    fun getAll(): List<ExerciseOnHoldDto> = transaction {
        ExercisesOnHold.selectAll().map(::resultRowTo)
    }

    fun getById(id: Int): ExerciseOnHoldDto? = transaction {
        ExercisesOnHold.selectAll().where { ExercisesOnHold.id eq id }.singleOrNull()?.let(::resultRowTo)
    }

    fun create(dto: CreateExerciseOnHoldDto): ExerciseOnHoldDto = try {
        transaction {
            val newId = ExercisesOnHold.insert {
                it[exerciseId] = dto.exerciseId
                it[userId] = dto.userId

            }[ExercisesOnHold.id]

            ExerciseOnHoldDto(
                id = newId.value,
                exerciseId = dto.exerciseId,
                userId = dto.userId,
                failureDate = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear exerciseOnHold: ${e.message}")
    }

    fun update(id: Int, dto: UpdateExerciseOnHoldDto) {
        transaction {
            getById(id) ?: throw BadRequestException("ExerciseOnHold con ID $id no existe.")
            ExercisesOnHold.update({ ExercisesOnHold.id eq id }) { u ->
                dto.exerciseId?.let { u[exerciseId] = it }
                dto.userId?.let { u[userId] = it }

            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("ExerciseOnHold con ID $id no existe.")
        ExercisesOnHold.deleteWhere { ExercisesOnHold.id eq id } > 0
    }
}
