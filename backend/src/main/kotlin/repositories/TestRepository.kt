package repositories

import com.example.dtos.CreateTestCompletedDto
import com.example.dtos.CreateTestDto
import com.example.dtos.FilterTestsDto
import com.example.dtos.TestCompletedDto
import com.example.dtos.TestDto
import com.example.dtos.UpdateTestCompletedDto
import com.example.dtos.UpdateTestDto
import io.ktor.server.plugins.BadRequestException
import models.Tests
import models.TestCompleted
import models.Exercises
import models.ExerciseCompleted
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.andWhere
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
            isActive = row[Tests.isActive],
            createdAt = row[Tests.createdAt].toString()
        )
    }

    private fun resultRowToTestCompleted(row: ResultRow): TestCompletedDto {
        return TestCompletedDto(
            id = row[TestCompleted.id].value,
            userId = row[TestCompleted.userId],
            testId = row[TestCompleted.testId],
            score = row[TestCompleted.score],
            completedAt = row[TestCompleted.completionDate].toString()
        )
    }

    fun getAllTests(): List<TestDto> = transaction {
        Tests.selectAll().orderBy(Tests.createdAt).map(::resultRowToTest)
    }

    fun getTestById(id: Int): TestDto? = transaction {
        Tests.selectAll().where { Tests.id eq id }.singleOrNull()?.let(::resultRowToTest)
    }

    fun getTestsByUnitId(unitId: Int): TestDto? = transaction {
        Tests.selectAll().where { Tests.unitId eq unitId }.singleOrNull()?.let(::resultRowToTest)
    }


    fun createTest(dto: CreateTestDto): TestDto = try {
        transaction {

            val newId = Tests.insert {
                it[unitId] = dto.unitId
                it[name] = dto.name
                it[description] = dto.description
                it[isActive] = dto.isActive ?: false
            }[Tests.id]

            TestDto(
                id = newId.value,
                unitId = dto.unitId,
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
                dto.name?.let { update[name] = it }
                dto.description?.let { update[description] = it }
                dto.isActive?.let { update[isActive] = it }
            }
        }
    }

    fun deleteTest(id: Int): Boolean = transaction {
        getTestById(id) ?: throw BadRequestException("El test con ID $id no existe.")
        Tests.deleteWhere { Tests.id eq id } > 0
    }

    fun filterTests(filters: FilterTestsDto): List<TestDto> = transaction {
        var query = Tests.selectAll()

        filters.name?.takeIf { it.isNotBlank() }?.let { query = query.andWhere { Tests.name like "%$it%" } }
        filters.unitId?.let { query = query.andWhere { Tests.unitId eq it } }
        filters.isActive?.let { query = query.andWhere { Tests.isActive eq it } }

        query.orderBy(Tests.createdAt).map(::resultRowToTest)
    }


    /* TestCompleted operations */

    fun getTestCompletedById(id: Int): TestCompletedDto? = transaction {
        TestCompleted.selectAll().where { TestCompleted.id eq id }
            .singleOrNull()?.let(::resultRowToTestCompleted)
    }

    fun getAllTestCompleted(): List<TestCompletedDto> = transaction {
        TestCompleted.selectAll().map(::resultRowToTestCompleted)
    }

    fun getTestsCompletedByUser(userId: Int): List<TestCompletedDto> = transaction {
        TestCompleted.selectAll().where { TestCompleted.userId eq userId }
            .map(::resultRowToTestCompleted)
    }

    fun createTestCompleted(dto: CreateTestCompletedDto): TestCompletedDto = try {
        transaction {
            val newId = TestCompleted.insert {
                it[userId] = dto.userId
                it[testId] = dto.testId
                it[score] = dto.score
                it[completionDate] = LocalDateTime.now()
            }[TestCompleted.id]

            TestCompletedDto(
                id = newId.value,
                userId = dto.userId,
                testId = dto.testId,
                score = dto.score,
                completedAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear el test completado: ${e.message}")
    }

    fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto) {
        transaction {
            getTestCompletedById(id)
                ?: throw BadRequestException("El test completado con ID $id no existe.")
            TestCompleted.update({ TestCompleted.id eq id }) { update ->
                dto.userId?.let { update[userId] = it }
                dto.testId?.let { update[testId] = it }
                dto.score?.let { update[score] = it }
                update[completionDate] = LocalDateTime.now()
            }
        }
    }

    fun deleteTestCompleted(id: Int): Boolean = transaction {
        getTestCompletedById(id)
            ?: throw BadRequestException("El test completado con ID $id no existe.")
        TestCompleted.deleteWhere { TestCompleted.id eq id } > 0
    }

    // Calcula el estado de repaso para una unidad/test para un usuario
    fun getUnitReviewStatus(userId: Int, unitId: Int, testId: Int): com.example.dtos.UnitReviewStatusDto = transaction {
        // obtener último intento del test por este usuario
        val lastAttemptRow = TestCompleted.selectAll()
            .where { (TestCompleted.testId eq testId) and  (TestCompleted.userId eq userId) }
            .orderBy(TestCompleted.completionDate, SortOrder.DESC)
            .limit(1)
            .singleOrNull()

        val lastScore: Int? = lastAttemptRow?.get(TestCompleted.score)
        val lastAttemptAt: java.time.LocalDateTime? = lastAttemptRow?.get(TestCompleted.completionDate)

        // contar ejercicios activos en la unidad
        val totalExercisesInUnit = Exercises.selectAll()
            .where { Exercises.unitId eq unitId and Exercises.isActive.eq(true) }
            .count()
            .toInt()

        val required = when {
            totalExercisesInUnit <= 0 -> 1
            totalExercisesInUnit <= 2 -> 1
            totalExercisesInUnit <= 5 -> 2
            else -> kotlin.math.ceil(totalExercisesInUnit * 0.4f).toInt()
        }

        // seleccionar ejercicios completados por el usuario en esa unidad
        val completedRows = (ExerciseCompleted innerJoin Exercises)
            .selectAll()
            .where { (ExerciseCompleted.userId eq userId) and (Exercises.unitId eq unitId) }

        val reviewedSet: Set<Int> = completedRows.mapNotNull { row ->
            val completionDate = row[ExerciseCompleted.completionDate]
            val exerciseId = row[ExerciseCompleted.exerciseId]
            if (lastAttemptAt != null) {
                if (completionDate.isAfter(lastAttemptAt)) exerciseId else null
            } else {
                exerciseId
            }
        }.toSet()

        val reviewed = reviewedSet.size
        val remaining = (required - reviewed).coerceAtLeast(0)
        val requiresReview = (lastScore ?: 0) != 100 && remaining > 0

        com.example.dtos.UnitReviewStatusDto(
            userId = userId,
            unitId = unitId,
            testId = testId,
            requiresReview = requiresReview,
            requiredExercises = required,
            reviewedExercises = reviewed,
            remainingExercises = remaining,
            lastAttemptScore = lastScore,
            lastAttemptAt = lastAttemptAt?.toString()
        )
    }
}
