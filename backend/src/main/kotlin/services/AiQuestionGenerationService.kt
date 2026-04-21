package com.example.services

import com.example.dtos.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException

class AiQuestionGenerationService(
    private val exerciseContentService: ExerciseContentService,
    private val questionService: QuestionService,
    private val aiClient: QuestionAIClientService
) {

    suspend fun generateForContent(
        contentId: Int,
        request: GenerateQuestionsFromAiRequestDto
    ): GenerateQuestionsFromAiResponseDto {
        if (request.questionCount <= 0) throw BadRequestException("questionCount debe ser > 0")

        val content = exerciseContentService.getById(contentId) ?: throw NotFoundException("ExerciseContent $contentId no existe")

        val aiResponse = aiClient.generateQuestions(
            AiGenerationRequestDto(
                contentId = content.id,
                exerciseId = content.exerciseId,
                textContent = content.textContent,
                grammarExplanation = content.grammarExplanation,
                questionCount = request.questionCount,
                language = request.language,
                difficulty = request.difficulty
            )
        )

        val rejected = mutableListOf<RejectedAiQuestionDto>()
        val created = mutableListOf<QuestionWithAlternativesDto>()

        aiResponse.questions.forEachIndexed { index, q ->
            val reason = validateQuestion(q)
            if (reason != null) {
                rejected.add(RejectedAiQuestionDto(index, reason))
                return@forEachIndexed
            }

            val question = questionService.createQuestion(
                contentId,
                CreateQuestionDto(questionText = q.questionText, isActive = true)
            )

            val alternatives = q.alternatives.map {
                questionService.createAlternative(
                    question.id,
                    CreateAlternativeDto(text = it.text, isCorrect = it.isCorrect)
                )
            }

            created.add(QuestionWithAlternativesDto(question = question, alternatives = alternatives))
        }

        return GenerateQuestionsFromAiResponseDto(
            contentId = contentId,
            accepted = created.size,
            rejected = rejected,
            createdQuestions = created
        )
    }

    private fun validateQuestion(q: AiGeneratedQuestionDto): String? {
        if (q.questionText.isBlank()) return "questionText_vacio"
        if (q.alternatives.size < 2) return "alternativas_insuficientes"
        if (q.alternatives.any { it.text.isBlank() }) return "alternativa_vacia"
        val correctCount = q.alternatives.count { it.isCorrect }
        if (correctCount != 1) return "debe_haber_una_sola_correcta"
        val normalized = q.alternatives.map { it.text.trim().lowercase() }
        if (normalized.size != normalized.toSet().size) return "alternativas_duplicadas"
        return null
    }
}
