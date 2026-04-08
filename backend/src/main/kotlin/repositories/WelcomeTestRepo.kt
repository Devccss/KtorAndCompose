package com.example.repositories

import com.example.dtos.CreateWelcomeTestDto
import com.example.dtos.UpdateWelcomeTestDto
import com.example.dtos.WelcomeTestDto
import models.WelcomeTests
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class WelcomeTestRepo {

    private fun resultRowToWord(row: ResultRow): WelcomeTestDto {
        return WelcomeTestDto(
            testId = row[WelcomeTests.testId],
            isActive = row[WelcomeTests.isActive],
            createAt = row[WelcomeTests.createdAt].toString()
        )
    }

     fun getAll(): List<WelcomeTestDto> = transaction {
        WelcomeTests.selectAll().orderBy(WelcomeTests.createdAt).map(::resultRowToWord)
    }

    fun getByTestId(testId: Int): WelcomeTestDto? = transaction {
        WelcomeTests.selectAll().where { WelcomeTests.testId eq testId }.singleOrNull()?.let(::resultRowToWord)
    }
    fun createWelcomeTest(dto: CreateWelcomeTestDto): WelcomeTestDto = try {
        transaction {
            val newWelcomeTest = WelcomeTests.insert {
                it[WelcomeTests.testId] = dto.testId
                it[WelcomeTests.isActive] = dto.isActive
            }[WelcomeTests.testId]
            WelcomeTestDto(
                testId = newWelcomeTest,
                isActive = dto.isActive,
                createAt = LocalDateTime.now().toString()
            )
        }
    }catch (e: Exception) {
        throw Exception("Failed to create WelcomeTest: ${e.message}")
    }


    fun updateWelcomeTest(id: Int, dto: UpdateWelcomeTestDto): Boolean = transaction {
        val updateCount = WelcomeTests.update({ WelcomeTests.testId eq id }) { u->
            dto.testId?.let { u[WelcomeTests.testId] = it }
            dto.isActive?.let { u[WelcomeTests.isActive] = it }
        }
        updateCount > 0
    }
    fun deleteByTestId(testId: Int): Boolean = transaction {
        val deleteCount = WelcomeTests.deleteWhere { WelcomeTests.testId eq testId }
        deleteCount > 0
    }
}