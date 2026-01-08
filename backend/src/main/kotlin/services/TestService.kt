package com.example.services

import com.example.dtos.CreateTestDto
import com.example.dtos.TestDto
import com.example.dtos.UpdateTestDto
import repositories.TestRepository

class TestService(private val repo: TestRepository) {
    fun getAll(): List<TestDto> = repo.getAll()
    fun getById(id: Int): TestDto? = repo.getById(id)
    fun create(dto: CreateTestDto): TestDto = repo.create(dto)
    fun update(id: Int, dto: UpdateTestDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
