package org.example.project.service

import org.example.project.dtos.AiGeneratedAlternativeDto
import org.example.project.dtos.ConfirmAiQuestionRequestDto
import org.example.project.dtos.ConfirmAiQuestionResponseDto
import org.example.project.dtos.GenerateQuestionsFromAiRequestDto
import org.example.project.dtos.GenerateQuestionsFromAiResponseDto
import org.example.project.repository.AiQuestionGenerationRepo

class AiQuestionGenerationService(
    private val repository: AiQuestionGenerationRepo
) {

    suspend fun generateQuestions(
        contentId: Int,
        request: GenerateQuestionsFromAiRequestDto
    ): GenerateQuestionsFromAiResponseDto = repository.generateQuestions(contentId, request)

    suspend fun generateQuestions(
        contentId: Int,
        questionCount: Int = 1,
        language: String = "es",
        difficulty: String? = null
    ): GenerateQuestionsFromAiResponseDto = repository.generateQuestions(
        contentId,
        GenerateQuestionsFromAiRequestDto(
            questionCount = questionCount,
            language = language,
            difficulty = difficulty
        )
    )

    suspend fun confirmQuestion(
        contentId: Int,
        request: ConfirmAiQuestionRequestDto
    ): ConfirmAiQuestionResponseDto = repository.confirmQuestion(contentId, request)

    suspend fun confirmQuestion(
        contentId: Int,
        questionText: String,
        alternatives: List<AiGeneratedAlternativeDto>,
        isActive: Boolean = true
    ): ConfirmAiQuestionResponseDto = repository.confirmQuestion(
        contentId,
        ConfirmAiQuestionRequestDto(
            questionText = questionText,
            alternatives = alternatives,
            isActive = isActive
        )
    )
}

