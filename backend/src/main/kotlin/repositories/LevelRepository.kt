package repositories

import DifficultyLevel
import LevelCreationDTO
import LevelDTO
import LevelUpdateDTO
import Levels
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

// En backend/src/main/kotlin/repositories/Repositories.LevelRepository.kt


class LevelRepository {

    private fun resultRowToLevel(row: ResultRow): LevelDTO {
        return LevelDTO(
            id = row[Levels.id].value,
            accent = row[Levels.accent],
            difficulty = row[Levels.difficulty],
            name = row[Levels.name],
            description = row[Levels.description],
            order = row[Levels.order],
            isActive = row[Levels.isActive],
            createdAt = row[Levels.createdAt].toString()
        )
    }

    suspend fun getAllLevels(): List<LevelDTO> = newSuspendedTransaction {
        Levels.selectAll().orderBy(Levels.order).map(::resultRowToLevel)
    }

    suspend fun getLevelById(id: Int): LevelDTO? = newSuspendedTransaction {
        Levels.selectAll().where { Levels.id eq id }.singleOrNull()?.let(::resultRowToLevel)
    }

    suspend fun createLevel(level: LevelCreationDTO): LevelDTO = newSuspendedTransaction {
        try {
            require(level.name.isNotBlank()) { "El nombre no puede estar vacío" }
            require(level.description.isNotBlank()) { "La descripción no puede estar vacía" }
            require(level.order >= 0) { "El orden debe ser un número positivo" }

            val insertedId = Levels.insert {
                it[accent] = level.accent
                it[difficulty] = level.difficulty
                it[name] = level.name
                it[description] = level.description
                it[order] = level.order
            }[Levels.id]

            LevelDTO(
                id = insertedId.value,
                accent = level.accent,
                difficulty = level.difficulty,
                name = level.name,
                description = level.description,
                order = level.order,
                isActive = true, // valor por defecto
                createdAt = LocalDateTime.now().toString() // o el valor que corresponda
            )

        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw e.message?.let { BadRequestException(it) }!!
                else -> {
                    println("Error al crear nivel: ${e.stackTraceToString()}")
                    throw e
                }
            }
        }
    }

    suspend fun updateLevel(id: Int, level: LevelUpdateDTO): Boolean = newSuspendedTransaction {
        Levels.update({ Levels.id eq id }) { update ->
            level.accent?.let { update[accent] = it }
            level.difficulty?.let { update[difficulty] = it }
            level.name?.let { update[name] = it }
            level.description?.let { update[description] = it }
            level.isActive?.let { update[isActive] = it }
            level.order?.let { update[order] = it }
        } > 0
    }

    suspend fun deleteLevel(id: Int): Boolean = newSuspendedTransaction {
        Levels.deleteWhere { Levels.id eq id } > 0
    }

    suspend fun getLevelsByDifficulty(difficulty: DifficultyLevel): List<LevelDTO> =
        newSuspendedTransaction {
            Levels.selectAll().where { Levels.difficulty eq difficulty }
                .orderBy(Levels.order)
                .map(::resultRowToLevel)
        }
}