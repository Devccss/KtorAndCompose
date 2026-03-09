package repositories

import com.example.dtos.CreateUnitCompletedDto
import com.example.dtos.CreateUnitDto
import com.example.dtos.FilterUnitsDto
import com.example.dtos.UnitCompletedDto
import com.example.dtos.UnitDto
import com.example.dtos.UpdateUnitCompletedDto
import com.example.dtos.UpdateUnitDto
import io.ktor.server.plugins.BadRequestException
import models.Units
import models.DifficultyLevel
import models.UnitsCompleted

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class UnitRepository {

    private fun resultRowToUnit(row: ResultRow): UnitDto {
        return UnitDto(
            id = row[Units.id].value,
            difficulty = row[Units.difficulty],
            name = row[Units.name],
            description = row[Units.description],
            orderUnit = row[Units.orderUnit],
            isActive = row[Units.isActive],
            createdAt = row[Units.createdAt].toString()
        )
    }

    private fun resultRowToUnitCompleted(row: ResultRow): UnitCompletedDto {
        return UnitCompletedDto(
            id = row[UnitsCompleted.id].value,
            userId = row[UnitsCompleted.userId],
            unitId = row[UnitsCompleted.unitId],
            completedAt = row[UnitsCompleted.completionDate].toString()
        )
    }

    fun getAllUnits(): List<UnitDto> = transaction {
        Units.selectAll().orderBy(Units.createdAt).map(::resultRowToUnit)
    }

    fun getUnitById(id: Int): UnitDto? = transaction {
        Units.selectAll().where { Units.id eq id }.singleOrNull()?.let(::resultRowToUnit)
    }

    fun getUnitsByDifficulty(difficulty: DifficultyLevel): List<UnitDto> = transaction {
        Units.selectAll().where { Units.difficulty eq difficulty }.map(::resultRowToUnit)
    }

    fun searchUnits(filters: FilterUnitsDto): List<UnitDto> =
        transaction {
            var query = Units.selectAll()

            filters.name?.let {
                query = query.andWhere { Units.name like "%$it%" }
            }
            filters.difficulty?.let {
                query = query.andWhere { Units.difficulty eq it }
            }
            filters.isActive?.let {
                query = query.andWhere { Units.isActive eq it }
            }

            return@transaction query.orderBy(Units.createdAt).map(::resultRowToUnit)
        }


    fun createUnit(dto: CreateUnitDto): UnitDto = try {
        transaction {
            var order = 0
            if (dto.orderUnit == null) {
                val total = (Units.selectAll().count() + 1)
                order = total.toInt()
            }
            val newId = Units.insert {
                it[difficulty] = dto.difficulty
                it[name] = dto.name
                it[description] = dto.description
                it[orderUnit] = dto.orderUnit ?: order
                it[isActive] = dto.isActive ?: false
            }[Units.id]

            UnitDto(
                id = newId.value,
                difficulty = dto.difficulty,
                name = dto.name,
                description = dto.description,
                orderUnit = dto.orderUnit ?: order,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la unidad: ${e.message}")
    }

    fun updateUnit(id: Int, dto: UpdateUnitDto) {
        transaction {
            getUnitById(id) ?: throw BadRequestException("La unidad con ID $id no existe.")

            Units.update({ Units.id eq id }) { update ->
                dto.name?.let { update[name] = it }
                dto.difficulty?.let { update[difficulty] = it }
                dto.description?.let { update[description] = it }
                dto.orderUnit?.let { update[orderUnit] = it }
                dto.isActive?.let { update[isActive] = it }
            }
        }
    }

    fun reorderUnits(idOrder: List<Pair<Int, Int>>): Boolean = transaction {
        try {
            idOrder.forEach { (id, _) ->
                Units.update({ Units.id eq id }) {
                    it[orderUnit] = -id
                }
            }


            idOrder.forEach { (id, order) ->
                Units.update({ Units.id eq id }) {
                    it[orderUnit] = order
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            rollback() // Revertir cambios si algo falla
            throw BadRequestException("Error al reordenar las unidades: ${e.message}")
        }
    }


    fun deleteUnit(id: Int): Boolean = transaction {
        getUnitById(id) ?: throw BadRequestException("La unidad con ID $id no existe.")
        Units.deleteWhere { Units.id eq id } > 0
    }

    fun createUnitsCompleted(dto: CreateUnitCompletedDto): UnitCompletedDto = try {
        transaction {
            val newId = UnitsCompleted.insert {
                it[userId] = dto.userId
                it[unitId] = dto.unitId
                it[completionDate] = LocalDateTime.now()
            }[UnitsCompleted.id]

            UnitCompletedDto(
                id = newId.value,
                userId = dto.userId,
                unitId = dto.unitId,
                completedAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la unidad completada: ${e.message}")
    }

    fun getUnitsCompletedByUser(userId: Int): List<UnitCompletedDto> = transaction {
        UnitsCompleted.selectAll().map(::resultRowToUnitCompleted)
    }

    fun editUnitsCompleted(id: Int, dto: UpdateUnitCompletedDto) {
        transaction {
            getUnitsCompletedByUser(id)

            UnitsCompleted.update({ UnitsCompleted.id eq id }) { update ->
                dto.userId?.let { update[userId] = it }
                dto.unitId?.let { update[unitId] = it }
                update[completionDate] = LocalDateTime.now()
            }
        }
    }


    fun getUnitsCompletedById(id: Int): UnitCompletedDto? = transaction {
        UnitsCompleted.selectAll().where { UnitsCompleted.id eq id }.singleOrNull()
            ?.let(::resultRowToUnitCompleted)
    }

    fun getAllUnitsCompleted(): List<UnitCompletedDto> = transaction {
        UnitsCompleted.selectAll().map(::resultRowToUnitCompleted)
    }

    fun deleteUnitsCompleted(id: Int): Boolean = transaction {
        getUnitsCompletedById(id)
            ?: throw BadRequestException("La unidad completada con ID $id no existe.")
        UnitsCompleted.deleteWhere { UnitsCompleted.id eq id } > 0
    }
}