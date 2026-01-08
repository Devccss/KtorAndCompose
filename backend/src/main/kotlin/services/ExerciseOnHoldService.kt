package com.example.services

import com.example.dtos.CreateExerciseOnHoldDto
import com.example.dtos.ExerciseOnHoldDto
import com.example.dtos.UpdateExerciseOnHoldDto
import repositories.ExerciseOnHoldRepository

class ExerciseOnHoldService(private val repo: ExerciseOnHoldRepository) {
    fun getAll(): List<ExerciseOnHoldDto> = repo.getAll()
    fun getById(id: Int): ExerciseOnHoldDto? = repo.getById(id)
    fun create(dto: CreateExerciseOnHoldDto): ExerciseOnHoldDto = repo.create(dto)
    fun update(id: Int, dto: UpdateExerciseOnHoldDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
