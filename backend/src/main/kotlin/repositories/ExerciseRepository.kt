package repositories

import com.example.dtos.CreateExerciseCompletedDto
import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseCompletedDto
import com.example.dtos.ExerciseDto
import com.example.dtos.FilterExercisesDto
import com.example.dtos.UpdateExerciseCompletedDto
import com.example.dtos.UpdateExerciseDto
import io.ktor.server.plugins.BadRequestException
import models.ExerciseCompleted
import models.Exercises

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.andWhere
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
            orderExercise = row[Exercises.orderExercise],
            isActive = row[Exercises.isActive],
            createdAt = row[Exercises.createdAt].toString()
        )
    }

    fun getAll(): List<ExerciseDto> = transaction {
        Exercises.selectAll().orderBy(Exercises.orderExercise).map(::resultRowToExercise)
    }

    fun searchExercises(filters: FilterExercisesDto): List<ExerciseDto> = transaction {
            var query = Exercises.selectAll()

            filters.name?.let {
                query = query.andWhere { Exercises.name like "%$it%" }
            }
            filters.isActive?.let {
                query = query.andWhere { Exercises.isActive eq it }
            }
            filters.unitId?.let {
                query = query.andWhere { Exercises.unitId eq it }
            }

            query.orderBy(Exercises.orderExercise).map(::resultRowToExercise)
        }

    fun getById(id: Int): ExerciseDto? = transaction {
        Exercises.selectAll().where { Exercises.id eq id }.singleOrNull()
            ?.let(::resultRowToExercise)
    }

    fun getByUnitId(unitId: Int): List<ExerciseDto> = transaction {
        Exercises.selectAll().where { Exercises.unitId eq unitId }.orderBy(Exercises.createdAt)
            .map(::resultRowToExercise)
    }

    fun create(dto: CreateExerciseDto): ExerciseDto = try {
        transaction {

            val total = Exercises.selectAll().count()

            val newId = Exercises.insert {
                it[unitId] = dto.unitId
                it[name] = dto.name
                it[description] = dto.description
                it[orderExercise] = dto.orderExercise ?: (total.toInt() + 1)
                it[isActive] = dto.isActive ?: false
                it[createdAt] = LocalDateTime.now()
            }[Exercises.id]

            ExerciseDto(
                id = newId.value,
                unitId = dto.unitId,
                name = dto.name,
                description = dto.description,
                isActive = dto.isActive ?: false,
                orderExercise = dto.orderExercise ?: (total.toInt() + 1),
                createdAt = LocalDateTime.now().toString(),
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el exercise: ${e.message}")
    }

    fun update(id: Int, dto: UpdateExerciseDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Exercise con ID $id no existe.")
            Exercises.update({ Exercises.id eq id }) { u ->
                dto.unitId?.let { u[unitId] = it }
                dto.name?.let { u[name] = it }
                dto.description?.let { u[description] = it }
                dto.isActive?.let { u[isActive] = it }
            }
            return@transaction true
        }
    }

    fun reorderExercises(newOrders: List<Pair<Int, Int>>): Boolean = transaction {
        try {
            newOrders.forEach { (id, _) ->
                Exercises.update({ Exercises.id eq id }) {
                    it[orderExercise] = -id
                }
            }
            newOrders.forEach { (id, order) ->
                Exercises.update({ Exercises.id eq id }) {
                    it[orderExercise] = order
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            rollback() // Revertir cambios si algo falla
            throw BadRequestException("Error al reordenar los ejercicios: ${e.message}")
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

    fun getAllExercisesCompletedByStudents(): List<ExerciseCompletedDto> = transaction {
        (ExerciseCompleted innerJoin models.Users).selectAll()
            .where { models.Users.role eq models.Role.STUDENT }
            .map { row ->
                ExerciseCompletedDto(
                    id = row[ExerciseCompleted.id].value,
                    userId = row[ExerciseCompleted.userId],
                    exerciseId = row[ExerciseCompleted.exerciseId],
                    completedAt = row[ExerciseCompleted.completionDate].toString()
                )
            }
    }

    fun createExerciseCompleted(dto: CreateExerciseCompletedDto): ExerciseCompletedDto =
        transaction {
            val exists = ExerciseCompleted.selectAll().where {
                (ExerciseCompleted.userId eq dto.userId) and
                        (ExerciseCompleted.exerciseId eq dto.exerciseId)
            }.count() > 50

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


    fun updateExerciseCompleted(id: Int, dto: UpdateExerciseCompletedDto) {
        transaction {
            getExerciseCompletedById(id)
                ?: throw BadRequestException("El registro de ejercicio completado con ID $id no existe.")

            ExerciseCompleted.update({ ExerciseCompleted.id eq id }) { update ->
                dto.userId?.let { update[userId] = it }
                dto.exerciseId?.let { update[exerciseId] = it }
                update[completionDate] = LocalDateTime.now()
            }
        }
    }

    fun deleteExerciseCompleted(id: Int): Boolean = transaction {
        getExerciseCompletedById(id)
            ?: throw BadRequestException("El registro de ejercicio completado con ID $id no existe.")
        ExerciseCompleted.deleteWhere { ExerciseCompleted.id eq id } > 0
    }

}
