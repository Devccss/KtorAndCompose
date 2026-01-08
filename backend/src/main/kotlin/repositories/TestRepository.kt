package repositories

import com.example.dtos.CreateTestDto
import com.example.dtos.TestDto
import com.example.dtos.UpdateTestDto
import io.ktor.server.plugins.BadRequestException
import models.Tests
import models.TestType

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class TestRepository {

    private fun resultRowToTest(row: ResultRow): TestDto {
        return TestDto(
            id = row[Tests.id].value,
            unitId = row[Tests.unitId],
            name = row[Tests.name],
            description = row[Tests.description],
            testType = row[Tests.testType],
            isActive = row[Tests.isActive],
            createdAt = row[Tests.createdAt].toString()
        )
    }

    fun getAll(): List<TestDto> = transaction {
        Tests.selectAll().orderBy(Tests.createdAt).map(::resultRowToTest)
    }

    fun getById(id: Int): TestDto? = transaction {
        Tests.selectAll().where { Tests.id eq id }.singleOrNull()?.let(::resultRowToTest)
    }

    fun create(dto: CreateTestDto): TestDto = try {
        transaction {
            val newId = Tests.insert {
                it[unitId] = dto.unitId
                it[name] = dto.name
                it[description] = dto.description
                it[testType] = dto.testType
                it[isActive] = dto.isActive ?: false
            }[Tests.id]

            TestDto(
                id = newId.value,
                unitId = dto.unitId,
                name = dto.name,
                description = dto.description,
                testType = dto.testType,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear test: ${e.message}")
    }

    fun update(id: Int, dto: UpdateTestDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Test con ID $id no existe.")
            Tests.update({ Tests.id eq id }) { u ->
                dto.unitId?.let { u[Tests.unitId] = it }
                dto.name?.let { u[Tests.name] = it }
                dto.description?.let { u[Tests.description] = it }
                dto.testType?.let { u[Tests.testType] = it }
                dto.isActive?.let { u[Tests.isActive] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Test con ID $id no existe.")
        Tests.deleteWhere { Tests.id eq id } > 0
    }
}
