package repositories

import com.example.dtos.CreateTestCompletedDto
import com.example.dtos.CreateTestDto
import com.example.dtos.TestCompletedDto
import com.example.dtos.TestDto
import com.example.dtos.UpdateTestCompletedDto
import com.example.dtos.UpdateTestDto
import io.ktor.server.plugins.BadRequestException
import models.Tests
import models.TestsCompleted
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
            name = row[Tests.name],
            description = row[Tests.description],
            isActive = row[Tests.isActive],
            createdAt = row[Tests.createdAt].toString()
        )
    }

    private fun resultRowToTestCompleted(row: ResultRow): TestCompletedDto {
        return TestCompletedDto(
            id = row[TestsCompleted.id].value,
            userId = row[TestsCompleted.userId],
            testId = row[TestsCompleted.testId],
            completedAt = row[TestsCompleted.completionDate].toString()
        )
    }

    fun getAllTests(): List<TestDto> = transaction {
        Tests.selectAll().orderBy(Tests.createdAt).map(::resultRowToTest)
    }

    fun getTestById(id: Int): TestDto? = transaction {
        Tests.selectAll().where { Tests.id eq id }.singleOrNull()?.let(::resultRowToTest)
    }

    fun createTest(dto: CreateTestDto): TestDto = try {
        transaction {
            val newId = Tests.insert {
                it[name] = dto.name
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
                // createdAt handled by DB or set now
            }[Tests.id]

            TestDto(
                id = newId.value,
                name = dto.name,
                description = dto.description,
                isActive = dto.isActive ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el test: ${e.message}")
    }

    fun updateTest(id: Int, dto: UpdateTestDto) {
        transaction {
            getTestById(id) ?: throw BadRequestException("El test con ID $id no existe.")
            Tests.update({ Tests.id eq id }) { update ->
                dto.name?.let { update[Tests.name] = it }
                dto.description?.let { update[Tests.description] = it }
                dto.isActive?.let { update[Tests.isActive] = it }
            }
        }
    }

    fun deleteTest(id: Int): Boolean = transaction {
        getTestById(id) ?: throw BadRequestException("El test con ID $id no existe.")
        Tests.deleteWhere { Tests.id eq id } > 0
    }

    /* TestCompleted operations */

    fun getTestCompletedById(id: Int): TestCompletedDto? = transaction {
        TestsCompleted.selectAll().where { TestsCompleted.id eq id }
            .singleOrNull()?.let(::resultRowToTestCompleted)
    }

    fun getAllTestCompleted(): List<TestCompletedDto> = transaction {
        TestsCompleted.selectAll().map(::resultRowToTestCompleted)
    }

    fun getTestsCompletedByUser(userId: Int): List<TestCompletedDto> = transaction {
        TestsCompleted.selectAll().where { TestsCompleted.userId eq userId }
            .map(::resultRowToTestCompleted)
    }

    fun createTestCompleted(dto: CreateTestCompletedDto): TestCompletedDto = try {
        transaction {
            val newId = TestsCompleted.insert {
                it[userId] = dto.userId
                it[testId] = dto.testId
                it[completionDate] = LocalDateTime.now()
            }[TestsCompleted.id]

            TestCompletedDto(
                id = newId.value,
                userId = dto.userId,
                testId = dto.testId,
                completedAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el test completado: ${e.message}")
    }

    fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto) {
        transaction {
            getTestCompletedById(id) ?: throw BadRequestException("El test completado con ID $id no existe.")
            TestsCompleted.update({ TestsCompleted.id eq id }) { update ->
                dto.userId?.let { update[TestsCompleted.userId] = it }
                dto.testId?.let { update[TestsCompleted.testId] = it }
                // always update completionDate to now if requested or to reflect edit
                update[TestsCompleted.completionDate] = LocalDateTime.now()
            }
        }
    }

    fun deleteTestCompleted(id: Int): Boolean = transaction {
        getTestCompletedById(id) ?: throw BadRequestException("El test completado con ID $id no existe.")
        TestsCompleted.deleteWhere { TestsCompleted.id eq id } > 0
    }
}
