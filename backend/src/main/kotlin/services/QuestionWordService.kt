package com.example.services
import com.example.dtos.CreateQuestionWordDto
import com.example.dtos.QuestionWordDto
import com.example.dtos.UpdateQuestionWordDto
import repositories.QuestionWordsRepository

class QuestionWordService(private val repo: QuestionWordsRepository) {
    fun getAll(): List<QuestionWordDto> = repo.getAll()
    fun getById(id: Int): QuestionWordDto? = repo.getById(id)
    fun create(dto: CreateQuestionWordDto): QuestionWordDto = repo.create(dto)
    fun update(id: Int, dto: UpdateQuestionWordDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
