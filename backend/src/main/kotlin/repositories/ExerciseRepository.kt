package repositories

import com.example.dtos.CreateExerciseCompletedDto
import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseCompletedDto
import com.example.dtos.ExerciseDto
import com.example.dtos.UpdateExerciseCompletedDto
import com.example.dtos.UpdateExerciseDto
import com.example.dtos.UpdateUnitCompletedDto
import io.ktor.server.plugins.BadRequestException
import models.ExerciseCompleted
import models.Exercises

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class ExerciseRepository {

    private fun resultRowToExercise(row: ResultRow): ExerciseDto {
        return ExerciseDto(
            id = row[Exercises.id].value,
            unitId = row[Exercises.unitId],
            name = row[Exercises.name],
            description = row[Exercises.description],
            isActive = row[Exercises.isActive],
            createdAt = row[Exercises.createdAt].toString()
        )
    }

    fun getAll(): List<ExerciseDto> = transaction {
        Exercises.selectAll().orderBy(Exercises.createdAt).map(::resultRowToExercise)
    }

    fun getById(id: Int): ExerciseDto? = transaction {
        Exercises.selectAll().where { Exercises.id eq id }.singleOrNull()?.let(::resultRowToExercise)
    }

    fun create(dto: CreateExerciseDto): ExerciseDto = try {
        transaction {
            val newId = Exercises.insert {
                it[unitId] = dto.unitId
                it[name] = dto.name
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
            }[Exercises.id]

            ExerciseDto(
                id = newId.value,
                unitId = dto.unitId,
                name = dto.name,
                description = dto.description,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el exercise: ${e.message}")
    }

    fun update(id: Int, dto: UpdateExerciseDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Exercise con ID $id no existe.")
            Exercises.update({ Exercises.id eq id }) { u ->
                dto.unitId?.let { u[Exercises.unitId] = it }
                dto.name?.let { u[Exercises.name] = it }
                dto.description?.let { u[Exercises.description] = it }
                dto.isActive?.let { u[Exercises.isActive] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Exercise con ID $id no existe.")
        Exercises.deleteWhere { Exercises.id eq id } > 0
    }

    fun getExerciseCompletedById(id: Int): ExerciseCompletedDto? = transaction {
        ExerciseCompleted.selectAll().where { ExerciseCompleted.id eq id }
            .singleOrNull()?.let { row ->
                ExerciseCompletedDto(
                    id = row[ExerciseCompleted.id].value,
                    userId = row[ExerciseCompleted.userId],
                    exerciseId = row[ExerciseCompleted.exerciseId],
                    completedAt = row[ExerciseCompleted.completionDate].toString()
                )
            }
    }
    fun getAllExerciseCompleted(): List<ExerciseCompletedDto> = transaction {
        ExerciseCompleted.selectAll()
            .map { row ->
                ExerciseCompletedDto(
                    id = row[ExerciseCompleted.id].value,
                    userId = row[ExerciseCompleted.userId],
                    exerciseId = row[ExerciseCompleted.exerciseId],
                    completedAt = row[ExerciseCompleted.completionDate].toString()
                )
            }
    }
    fun getExercisesCompletedByUser(userId: Int): List<ExerciseCompletedDto> = transaction {
        ExerciseCompleted.selectAll().where { ExerciseCompleted.userId eq userId }
            .map { row ->
                ExerciseCompletedDto(
                    id = row[ExerciseCompleted.id].value,
                    userId = row[ExerciseCompleted.userId],
                    exerciseId = row[ExerciseCompleted.exerciseId],
                    completedAt = row[ExerciseCompleted.completionDate].toString()
                )
            }
    }

    fun createExerciseCompleted(dto: CreateExerciseCompletedDto): ExerciseCompletedDto = try {
        transaction {
            val newId = ExerciseCompleted.insert {
                it[userId] = dto.userId
                it[exerciseId] = dto.exerciseId
                it[completionDate] = LocalDateTime.now()
            }[ExerciseCompleted.id]

            ExerciseCompletedDto(
                id = newId.value,
                userId = dto.userId,
                exerciseId = dto.exerciseId,
                completedAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el registro de ejercicio completado: ${e.message}")
    }
    fun updateExerciseCompleted(id: Int, dto: UpdateExerciseCompletedDto) {
        transaction {
            getExerciseCompletedById(id) ?: throw BadRequestException("El registro de ejercicio completado con ID $id no existe.")

            ExerciseCompleted.update({ ExerciseCompleted.id eq id }) { update ->
                dto.userId?.let { update[userId] = it }
                dto.exerciseId?.let { update[exerciseId] = it }
                update[completionDate] = LocalDateTime.now()
            }
        }
    }
    fun deleteExerciseCompleted(id: Int): Boolean = transaction {
        getExerciseCompletedById(id) ?: throw BadRequestException("El registro de ejercicio completado con ID $id no existe.")
        ExerciseCompleted.deleteWhere { ExerciseCompleted.id eq id } > 0
    }

}
