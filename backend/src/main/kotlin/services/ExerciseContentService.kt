package com.example.services

import com.example.dtos.CreateExerciseContentDto
import com.example.dtos.FilterExerciseContentDto
import com.example.dtos.UpdateExerciseContentDto
import com.example.repositories.ExerciseContentRepository

class ExerciseContentService(private val repo: ExerciseContentRepository) {
    fun createExerciseContent(exerciseId: Int, dto: CreateExerciseContentDto) = repo.createExerciseContent(exerciseId, dto)
    fun getByExerciseId(exerciseId: Int) = repo.getByExerciseId(exerciseId)
    fun getAllExerciseContent() = repo.getAllExerciseContent()

    fun getById(id: Int) = repo.getById(id)
    fun searchExerciseContent(filters: FilterExerciseContentDto) = repo.searchExerciseContent(filters)
    fun updateContentByExerciseId(exerciseId: Int, dto: UpdateExerciseContentDto) = repo.updateContentByExerciseId(exerciseId, dto)
    fun deleteByExerciseId(exerciseId: Int) = repo.deleteByExerciseId(exerciseId)
}