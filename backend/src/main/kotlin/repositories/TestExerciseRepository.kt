package repositories

import com.example.dtos.CreateTestExerciseDto
import com.example.dtos.TestExerciseDto
import com.example.dtos.UpdateTestExerciseDto
import io.ktor.server.plugins.BadRequestException
import models.TestExercises

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class TestExerciseRepository {

    private fun resultRowToTestExercise(row: ResultRow): TestExerciseDto {
        return TestExerciseDto(
            id = row[TestExercises.id].value,
            testId = row[TestExercises.testId],
            exerciseId = row[TestExercises.exerciseId]
        )
    }

    fun getAll(): List<TestExerciseDto> = transaction {
        TestExercises.selectAll().map(::resultRowToTestExercise)
    }

    fun getById(id: Int): TestExerciseDto? = transaction {
        TestExercises.selectAll().where { TestExercises.id eq id }.singleOrNull()?.let(::resultRowToTestExercise)
    }

    fun searchByIds(testId: Int? = null, exerciseId: Int? = null): List<TestExerciseDto> = transaction {
        var query = TestExercises.selectAll()
        testId?.let { query = query.where { TestExercises.testId eq it } }
        exerciseId?.let { query = query.where { TestExercises.exerciseId eq it } }
        query.map(::resultRowToTestExercise)
    }

    fun create(dto: CreateTestExerciseDto): TestExerciseDto = try {
        transaction {
            val newId = TestExercises.insert {
                it[testId] = dto.testId
                it[exerciseId] = dto.exerciseId
            }[TestExercises.id]

            TestExerciseDto(id = newId.value, testId = dto.testId, exerciseId = dto.exerciseId)
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear testExercise: ${e.message}")
    }

    fun update(id: Int, dto: UpdateTestExerciseDto) {
        transaction {
            getById(id) ?: throw BadRequestException("TestExercise con ID $id no existe.")
            TestExercises.update({ TestExercises.id eq id }) { u ->
                dto.testId?.let { u[TestExercises.testId] = it }
                dto.exerciseId?.let { u[TestExercises.exerciseId] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        val testExerciseId = searchByIds(exerciseId = id)
        testExerciseId.forEach { testExId->
            TestExercises.deleteWhere { TestExercises.id eq testExId.id }
        }
         true
    }
}
