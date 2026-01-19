package com.example.services

import com.example.dtos.CreateTestCompletedDto
import com.example.dtos.CreateTestDto
import com.example.dtos.TestCompletedDto
import com.example.dtos.TestDto
import com.example.dtos.UpdateTestCompletedDto
import com.example.dtos.UpdateTestDto
import repositories.TestRepository

class TestService(private val repo: TestRepository) {
    fun getAll(): List<TestDto> = repo.getAllTests()
    fun getById(id: Int): TestDto? = repo.getTestById(id)
    fun create(dto: CreateTestDto): TestDto = repo.createTest(dto)
    fun update(id: Int, dto: UpdateTestDto) = repo.updateTest(id, dto)
    fun delete(id: Int): Boolean = repo.deleteTest(id)

    fun createTestCompleted(dto: CreateTestCompletedDto): TestCompletedDto = repo.createTestCompleted(dto)
    fun getAllTestCompleted(): List<TestCompletedDto> = repo.getAllTestCompleted()
    fun getTestCompletedById(id: Int): TestCompletedDto? = repo.getTestCompletedById(id)
    fun getTestsCompletedByUser(userId: Int): List<TestCompletedDto> = repo.getTestsCompletedByUser(userId)
    fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto) = repo.updateTestCompleted(id, dto)
    fun deleteTestCompleted(id: Int): Boolean = repo.deleteTestCompleted(id)
}
