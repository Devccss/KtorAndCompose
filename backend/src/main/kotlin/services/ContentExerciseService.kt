package com.example.services

import com.example.dtos.ContentExerciseDto
import com.example.dtos.CreateContentExerciseDto
import com.example.dtos.UpdateContentExerciseDto
import repositories.ContentExerciseRepository

class ContentExerciseService(private val repo: ContentExerciseRepository) {
    fun getAll(): List<ContentExerciseDto> = repo.getAll()
    fun getById(id: Int): ContentExerciseDto? = repo.getById(id)
    fun create(dto: CreateContentExerciseDto): ContentExerciseDto = repo.create(dto)
    fun update(id: Int, dto: UpdateContentExerciseDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
