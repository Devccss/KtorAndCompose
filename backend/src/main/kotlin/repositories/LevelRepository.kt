package repositories

import LevelCreationDTO
import LevelDTO
import LevelUpdateDTO
import models.DifficultyLevel
import models.Units
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

fun resultRowToLevel(row: ResultRow): LevelDTO {
    return LevelDTO(
        id = row[Units.id].value,
        difficulty = row[Units.difficulty],
        name = row[Units.name],
        description = row[Units.description],
        orderLevel = row[Units.orderUnit],
        isActive = row[Units.isActive],
        createdAt = row[Units.createdAt].toString()
    )
}

class LevelRepository(private val dialogRepository: DialogRepository) {




    fun getAllLevels(): List<LevelDTO> = transaction {
        Units.selectAll().map(::resultRowToLevel)
    }

    fun getLevelById(id: Int): LevelDTO? = transaction {
        Units.selectAll().where { Units.id eq id }.singleOrNull()?.let(::resultRowToLevel)
    }

    fun createLevelSmart(
        dto: LevelCreationDTO,
        beforeId: Int? = null,
        afterId: Int? = null
    ): LevelDTO = try {
        transaction {
            require(dto.name.isNotBlank()) { "El nombre no puede estar vacío" }
            require(dto.description.isNotBlank()) { "La descripción no puede estar vacía" }

            val beforeOrder = Units
                .select(Units.orderUnit)
                .where(Units.id eq beforeId)
                .map { it[Units.orderUnit] }
                .singleOrNull()

            println("beforeOrderRepository: $beforeOrder")
            val afterOrder = afterId?.let {
                Units
                    .select(Units.orderUnit)
                    .where(Units.id eq afterId)
                    .map { it[Units.orderUnit] }
                    .singleOrNull()
            }
            println("afterOrderRepository: $afterOrder")

            val newOrder = when {
                beforeOrder != null && afterOrder != null -> (beforeOrder + afterOrder) / 2f
                beforeOrder != null -> beforeOrder + 1f
                afterOrder != null -> afterOrder - 1f
                else -> {
                    Units
                        .selectAll()
                        .filter { it[Units.difficulty] == dto.difficulty }
                        .map { it[Units.orderUnit] }
                        .maxOrNull()?.plus(1f) ?: 1f
                }
            }

            val insertedId = Units.insert {
                it[difficulty] = dto.difficulty
                it[name] = dto.name
                it[description] = dto.description
                it[orderUnit] = newOrder
            }[Units.id]

            LevelDTO(
                id = insertedId.value,
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
        Units.update({ Units.id eq id }) { update ->
            level.difficulty?.let { update[difficulty] = it }
            level.name?.let { update[name] = it }
            level.description?.let { update[description] = it }

            level.isActive?.let { update[isActive] = it }
            level.orderLevel?.let { update[orderUnit] = it }
        } > 0
    }

    fun deleteLevel(id: Int): Boolean = transaction {
        if (Units.select(Units.id eq id).empty()) {
            throw BadRequestException("No se encontró el nivel con ID: $id")
        }
        val dialogs = dialogRepository.getDialogsByLevelId(id)
        if (dialogs.isNotEmpty()) {
            throw BadRequestException("No se puede eliminar el nivel porque tiene diálogos asociados")
        }
        Units.deleteWhere { Units.id eq id } > 0
    }

    fun getLevelsByDifficulty(difficulty: DifficultyLevel): List<LevelDTO> = transaction {
        Units
            .selectAll()
            .where { Units.difficulty eq difficulty }
            .orderBy(Units.orderUnit)
            .map(::resultRowToLevel)
    }
}