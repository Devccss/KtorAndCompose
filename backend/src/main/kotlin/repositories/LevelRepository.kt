package repositories

import LevelCreationDTO
import LevelDTO
import LevelUpdateDTO
import models.DifficultyLevel
import models.Levels
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class LevelRepository(private val dialogRepository: DialogRepository) {


    private fun resultRowToLevel(row: ResultRow): LevelDTO {
        return LevelDTO(
            id = row[Levels.id].value,
            accent = row[Levels.accent],
            difficulty = row[Levels.difficulty],
            name = row[Levels.name],
            description = row[Levels.description],
            orderLevel = row[Levels.orderLevel],
            isActive = row[Levels.isActive],
            createdAt = row[Levels.createdAt].toString()
        )
    }

    fun getAllLevels(): List<LevelDTO> = transaction {
        Levels.selectAll().orderBy(Levels.orderLevel).map(::resultRowToLevel)
    }

    fun getLevelById(id: Int): LevelDTO? = transaction {
        Levels.selectAll().where { Levels.id eq id }.singleOrNull()?.let(::resultRowToLevel)
    }

    fun createLevelSmart(
        dto: LevelCreationDTO,
        beforeId: Int? = null,
        afterId: Int? = null
    ): LevelDTO = try {
        transaction {
            require(dto.name.isNotBlank()) { "El nombre no puede estar vacío" }
            require(dto.description.isNotBlank()) { "La descripción no puede estar vacía" }

            val newAccent = Levels
                .selectAll()
                .map { it[Levels.accent] }
                .maxOrNull()?.plus(1) ?: 1

            val beforeOrder = Levels
                .select(Levels.orderLevel)
                .where(Levels.id eq beforeId)
                .map { it[Levels.orderLevel] }
                .singleOrNull()

            println("beforeOrderRepository: $beforeOrder")
            val afterOrder = afterId?.let {
                Levels
                    .select(Levels.orderLevel)
                    .where(Levels.id eq afterId)
                    .map { it[Levels.orderLevel] }
                    .singleOrNull()
            }
            println("afterOrderRepository: $afterOrder")

            val newOrder = when {
                beforeOrder != null && afterOrder != null -> (beforeOrder + afterOrder) / 2f
                beforeOrder != null -> beforeOrder + 1f
                afterOrder != null -> afterOrder - 1f
                else -> {
                    Levels
                        .selectAll()
                        .filter { it[Levels.difficulty] == dto.difficulty }
                        .map { it[Levels.orderLevel] }
                        .maxOrNull()?.plus(1f) ?: 1f
                }
            }

            val insertedId = Levels.insert {
                it[accent] = newAccent
                it[difficulty] = dto.difficulty
                it[name] = dto.name
                it[description] = dto.description
                it[orderLevel] = newOrder
            }[Levels.id]

            LevelDTO(
                id = insertedId.value,
                accent = newAccent,
                difficulty = dto.difficulty,
                name = dto.name,
                description = dto.description,
                orderLevel = newOrder,
                isActive = true,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el nivel: ${e.message}")
    }

    fun updateLevel(id: Int, level: LevelUpdateDTO): Boolean = transaction {
        Levels.update({ Levels.id eq id }) { update ->
            level.accent?.let { update[accent] = it }
            level.difficulty?.let { update[difficulty] = it }
            level.name?.let { update[name] = it }
            level.description?.let { update[description] = it }

            level.isActive?.let { update[isActive] = it }
            level.orderLevel?.let { update[orderLevel] = it }
        } > 0
    }

    fun deleteLevel(id: Int): Boolean = transaction {
        if (Levels.select(Levels.id eq id).empty()) {
            throw BadRequestException("No se encontró el nivel con ID: $id")
        }
        val dialogs = dialogRepository.getDialogsByLevelId(id)
        if (dialogs.isNotEmpty()) {
            throw BadRequestException("No se puede eliminar el nivel porque tiene diálogos asociados")
        }
        Levels.deleteWhere { Levels.id eq id } > 0
    }

    fun getLevelsByDifficulty(difficulty: DifficultyLevel): List<LevelDTO> = transaction {
        Levels
            .selectAll()
            .where { Levels.difficulty eq difficulty }
            .orderBy(Levels.orderLevel)
            .map(::resultRowToLevel)
    }
}