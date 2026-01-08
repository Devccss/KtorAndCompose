package repositories

import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseDto
import com.example.dtos.UpdateExerciseDto
import io.ktor.server.plugins.BadRequestException
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
            levelId = row[Exercises.levelId],
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
                it[levelId] = dto.levelId
                it[name] = dto.name
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
            }[Exercises.id]

            ExerciseDto(
                id = newId.value,
                levelId = dto.levelId,
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
                dto.levelId?.let { u[Exercises.levelId] = it }
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
}
