package com.example.services
import com.example.dtos.CreateExerciseWordDto
import com.example.dtos.ExerciseWordDto
import com.example.dtos.UpdateExerciseWordDto
import repositories.QuestionWordsRepository

class ExerciseWordService(private val repo: QuestionWordsRepository) {
    fun getAll(): List<ExerciseWordDto> = repo.getAll()
    fun getById(id: Int): ExerciseWordDto? = repo.getById(id)
    fun getByExerciseId(exerciseId: Int): List<ExerciseWordDto> = repo.getByExerciseId(exerciseId)
    fun create(dto: CreateExerciseWordDto): ExerciseWordDto = repo.create(dto)
    fun update(id: Int, dto: UpdateExerciseWordDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
