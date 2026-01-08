package com.example.services

import com.example.dtos.CreateExerciseDto
import com.example.dtos.ExerciseDto
import com.example.dtos.UpdateExerciseDto
import repositories.ExerciseRepository

class ExerciseService(private val repo: ExerciseRepository) {
    fun getAll(): List<ExerciseDto> = repo.getAll()
    fun getById(id: Int): ExerciseDto? = repo.getById(id)
    fun create(dto: CreateExerciseDto): ExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateExerciseDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
