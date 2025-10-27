package com.example.repositories

import com.example.dtos.CreateTestDto
import com.example.dtos.TestDto
import models.DialogsTests
import models.TestType
import models.Tests
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class TestRepository {
    fun addDialogToTest(dialogId: Int, testId: Int):Boolean= transaction {
        val existing = DialogsTests.selectAll()
            .where { (DialogsTests.dialogId eq dialogId) and (DialogsTests.testId eq testId) }
            .singleOrNull()
        if (existing == null) {
            DialogsTests.insert {
                it[DialogsTests.dialogId] = dialogId
                it[DialogsTests.testId] = testId
            }
            return@transaction true
        }else{
            return@transaction false
        }
    }
    fun createTest(dto: CreateTestDto, levelId:Int ): TestDto = transaction {

        print("Created test: $dto")
        val newTest = Tests.insert {
            it[name] = dto.name
            it[description] = dto.description
            it[testType] = dto.testType?: TestType.TRANSLATION
            it[isActive] = dto.isActive?: false
            it[Tests.levelId] = levelId
        }[Tests.id]
        val test = TestDto(
            id = newTest.value,
            name = dto.name,
            description = dto.description,
            testType = dto.testType,
            isActive = dto.isActive?: false,
            levelId = levelId
        )
        print("Teste created: $test")
        return@transaction test
    }
    fun editTest(id: Int, dto: CreateTestDto): Boolean = transaction {
        val updatedRows = Tests.update({ Tests.id eq id }) {
            it[name] = dto.name
            it[description] = dto.description
            it[testType] = dto.testType?: TestType.TRANSLATION
        }
        return@transaction updatedRows>0

    }

    fun getTestById(id: Int): TestDto? = transaction {
        Tests.selectAll().where { Tests.id eq id }.singleOrNull()?.let {
            TestDto(
                id = it[Tests.id].value,
                name = it[Tests.name],
                description = it[Tests.description] ?: "",
                testType = it[Tests.testType],
                isActive = it[Tests.isActive],
                levelId = it[Tests.levelId]
            )
        }
    }
    fun getAllTests(): List<TestDto> = transaction {
        Tests.selectAll().map {
            TestDto(
                id = it[Tests.id].value,
                name = it[Tests.name],
                description = it[Tests.description] ?: "",
                testType = it[Tests.testType],
                isActive = it[Tests.isActive],
                levelId = it[Tests.levelId]
            )
        }
    }

    fun deleteTest(id: Int):Boolean = transaction {
        val dialogsTest = DialogsTests.select(DialogsTests.testId eq id).count()
        if (dialogsTest>0){
            return@transaction false
        }else{
            val deleted = Tests.deleteWhere { Tests.id eq id }
            return@transaction deleted > 0
        }
    }

    fun deleteDialogTest(dialogId: Int,testId:Int):Boolean = transaction {
        val existing = DialogsTests.deleteWhere { DialogsTests.dialogId eq dialogId and (DialogsTests.testId eq testId) }
        return@transaction existing >0
    }

}