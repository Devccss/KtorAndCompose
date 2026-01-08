package com.example.services

import com.example.dtos.CreateTestExerciseDto
import com.example.dtos.TestExerciseDto
import com.example.dtos.UpdateTestExerciseDto
import repositories.TestExerciseRepository

class TestExerciseService(private val repo: TestExerciseRepository) {
    fun getAll(): List<TestExerciseDto> = repo.getAll()
    fun getById(id: Int): TestExerciseDto? = repo.getById(id)
    fun create(dto: CreateTestExerciseDto): TestExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateTestExerciseDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
