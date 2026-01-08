package com.example.services

import com.example.dtos.CreateQuestionDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateQuestionDto
import repositories.QuestionRepository

class QuestionService(private val repo: QuestionRepository) {
    fun getAll(): List<QuestionDto> = repo.getAll()
    fun getById(id: Int): QuestionDto? = repo.getById(id)
    fun create(dto: CreateQuestionDto): QuestionDto = repo.create(dto)
    fun update(id: Int, dto: UpdateQuestionDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
