package com.example.services

import com.example.dtos.AlternativeDto
import com.example.dtos.CreateAlternativeDto
import com.example.dtos.CreateQuestionCompletedDto
import com.example.dtos.CreateQuestionDto
import com.example.dtos.QuestionCompletedDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateAlternativeDto
import com.example.dtos.UpdateQuestionCompletedDto
import com.example.dtos.UpdateQuestionDto
import repositories.QuestionRepository

class QuestionService(private val repo: QuestionRepository) {

    // Question CRUD
    fun getAllQuestions(): List<QuestionDto> = repo.getAllQuestions()
    fun getQuestionsByExerciseId(exerciseId: Int): List<QuestionDto> = repo.getQuestionsByExerciseId(exerciseId)
    fun getQuestionById(id: Int): QuestionDto? = repo.getQuestionById(id)

    fun createQuestion(exerciseId: Int,dto: CreateQuestionDto): QuestionDto = repo.createQuestion(exerciseId,dto)
    fun updateQuestion(id: Int, dto: UpdateQuestionDto) = repo.updateQuestion(id, dto)
    fun deleteQuestion(id: Int): Boolean = repo.deleteQuestion(id)

    //Alternatives
    fun getAlternativeByQuestionId(questionId: Int): List<AlternativeDto> = repo.getAlternativesByQuestionId(questionId)
    fun createAlternative(questionId: Int,dto: CreateAlternativeDto): AlternativeDto = repo.createAlternative(questionId,dto)
    fun updateAlternative(id: Int, dto: UpdateAlternativeDto) = repo.updateAlternative(id, dto)
    fun deleteAlternative(id: Int): Boolean = repo.deleteAlternative(id)


    // QuestionCompleted CRUD
    fun createQuestionCompleted(dto: CreateQuestionCompletedDto): QuestionCompletedDto = repo.createQuestionsCompleted(dto)
    fun getAllQuestionsCompleted(): List<QuestionCompletedDto> = repo.getAllQuestionsCompleted()
    fun getQuestionsCompletedByUser(userId: Int): List<QuestionCompletedDto> = repo.getQuestionsCompletedByUser(userId)
    fun getQuestionCompletedById(id: Int): QuestionCompletedDto? = repo.getQuestionsCompletedById(id)
    fun updateQuestionsCompleted(id: Int, dto: UpdateQuestionCompletedDto) = repo.updateQuestionsCompleted(id, dto)
    fun deleteQuestionsCompleted(id: Int): Boolean = repo.deleteQuestionsCompleted(id)
}
