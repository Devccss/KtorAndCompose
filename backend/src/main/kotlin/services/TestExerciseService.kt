package com.example.services

import com.example.dtos.CreateTestExerciseDto
import com.example.dtos.TestExerciseDto
import com.example.dtos.UpdateTestExerciseDto
import repositories.TestExerciseRepository

class TestExerciseService(private val repo: TestExerciseRepository) {
    fun getAll(): List<TestExerciseDto> = repo.getAll()
    fun getById(id: Int): TestExerciseDto? = repo.getById(id)
    fun searchByIds(testId: Int? = null, exerciseId: Int? = null): List<TestExerciseDto> = repo.searchByIds(testId, exerciseId)
    fun create(dto: CreateTestExerciseDto): TestExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateTestExerciseDto) = repo.update(id, dto)
    fun deleteByTestId(testId: Int): Boolean = repo.deleteByTestId(testId)
    fun deleteByExerciseId(exerciseId: Int): Boolean = repo.deleteByExerciseId(exerciseId)
}
