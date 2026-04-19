package com.example.services

import com.example.dtos.AlternativeDto
import com.example.dtos.CreateAlternativeDto
import com.example.dtos.CreateQuestionCompletedDto
import com.example.dtos.CreateQuestionDto
import com.example.dtos.FilterQuestionsDto
import com.example.dtos.QuestionCompletedDto
import com.example.dtos.QuestionDto
import com.example.dtos.UpdateAlternativeDto
import com.example.dtos.UpdateQuestionCompletedDto
import com.example.dtos.UpdateQuestionDto
import repositories.QuestionRepository

class QuestionService(private val repo: QuestionRepository) {

    // Question CRUD
    fun getAllQuestions(): List<QuestionDto> = repo.getAllQuestions()
    fun getQuestionsByExerciseId(contentId: Int): List<QuestionDto> = repo.getQuestionsByExerciseId(contentId)
    fun getQuestionById(id: Int): QuestionDto? = repo.getQuestionById(id)
    fun searchQuestions(filters: FilterQuestionsDto): List<QuestionDto> = repo.searchQuestions(filters)

    fun createQuestion(contentId: Int,dto: CreateQuestionDto): QuestionDto = repo.createQuestion(contentId,dto)
    fun updateQuestion(id: Int, dto: UpdateQuestionDto) = repo.updateQuestion(id, dto)
    fun deleteQuestion(id: Int): Boolean = repo.deleteQuestion(id)

    //Alternatives
    fun getAllAlternatives(): List<AlternativeDto> = repo.getAllAlternatives()
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
