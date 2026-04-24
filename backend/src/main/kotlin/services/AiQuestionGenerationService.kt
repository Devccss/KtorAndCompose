package com.example.services

import com.example.dtos.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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

        val difficultyElement: JsonElement = request.difficulty?.let { JsonPrimitive(it) } ?: JsonNull
        val aiRequestPayload = JsonObject(
            mapOf(
                "schemaVersion" to JsonPrimitive("1.0"),
                "contentId" to JsonPrimitive(content.id),
                "exerciseId" to JsonPrimitive(content.exerciseId),
                "textContent" to JsonPrimitive(content.textContent),
                "grammarExplanation" to JsonPrimitive(content.grammarExplanation),
                "questionCount" to JsonPrimitive(request.questionCount),
                "language" to JsonPrimitive(request.language),
                "difficulty" to difficultyElement
            )
        )

        val aiResponse = aiClient.generateQuestions(aiRequestPayload)
        val questions = aiResponse["questions"]?.asJsonArrayOrNull()
            ?: throw BadRequestException("La IA no devolvio el campo 'questions' en formato arreglo")

        val rejected = mutableListOf<RejectedAiQuestionDto>()
        val suggested = mutableListOf<AiGeneratedQuestionDto>()

        questions.forEachIndexed { index, questionElement ->
            val parsedQuestion = parseQuestion(questionElement)
            val reason = validateQuestion(parsedQuestion.questionText, parsedQuestion.alternatives)
            if (reason != null) {
                rejected.add(RejectedAiQuestionDto(index, reason))
                return@forEachIndexed
            }

            val alternatives = parsedQuestion.alternatives.map { (text, isCorrect) ->
                AiGeneratedAlternativeDto(text = text, isCorrect = isCorrect)
            }
            suggested.add(
                AiGeneratedQuestionDto(
                    questionText = parsedQuestion.questionText,
                    alternatives = alternatives
                )
            )
        }

        return GenerateQuestionsFromAiResponseDto(
            contentId = contentId,
            accepted = suggested.size,
            rejected = rejected,
            suggestedQuestions = suggested
        )
    }

    fun confirmForContent(
        contentId: Int,
        request: ConfirmAiQuestionRequestDto
    ): ConfirmAiQuestionResponseDto {
        exerciseContentService.getById(contentId)
            ?: throw NotFoundException("ExerciseContent $contentId no existe")

        val alternatives = request.alternatives.mapNotNull { alt ->
            val text = alt.text.trim()
            val isCorrect = alt.isCorrect
            if (text.isBlank()) null else text to isCorrect
        }

        val reason = validateQuestion(request.questionText.trim(), alternatives)
        if (reason != null) throw BadRequestException("Pregunta invalida: $reason")

        val createdQuestion = questionService.createQuestion(
            contentId,
            CreateQuestionDto(
                questionText = request.questionText.trim(),
                isActive = request.isActive
            )
        )

        val createdAlternatives = alternatives.map { (text, isCorrect) ->
            questionService.createAlternative(
                createdQuestion.id,
                CreateAlternativeDto(text = text, isCorrect = isCorrect)
            )
        }

        return ConfirmAiQuestionResponseDto(
            question = createdQuestion,
            alternatives = createdAlternatives
        )
    }

    private data class ParsedAiQuestion(
        val questionText: String,
        val alternatives: List<Pair<String, Boolean>>
    )

    private fun parseQuestion(questionElement: JsonElement): ParsedAiQuestion {
        val questionObject = questionElement.asJsonObjectOrNull() ?: JsonObject(emptyMap())
        val questionText = questionObject["questionText"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()

        val alternatives = questionObject["alternatives"]
            ?.asJsonArrayOrNull()
            ?.mapNotNull { alternativeElement ->
                val alternativeObject = alternativeElement.asJsonObjectOrNull() ?: return@mapNotNull null
                val text = alternativeObject["text"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val isCorrect = alternativeObject["isCorrect"]?.jsonPrimitive?.booleanOrNull
                if (text.isBlank() || isCorrect == null) null else text to isCorrect
            }
            ?: emptyList()

        return ParsedAiQuestion(questionText = questionText, alternatives = alternatives)
    }

    private fun validateQuestion(
        questionText: String,
        alternatives: List<Pair<String, Boolean>>
    ): String? {
        if (questionText.isBlank()) return "questionText_vacio"
        if (alternatives.size < 2) return "alternativas_insuficientes"
        if (alternatives.any { it.first.isBlank() }) return "alternativa_vacia"
        val correctCount = alternatives.count { it.second }
        if (correctCount != 1) return "debe_haber_una_sola_correcta"
        val normalized = alternatives.map { it.first.trim().lowercase() }
        if (normalized.size != normalized.toSet().size) return "alternativas_duplicadas"
        return null
    }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
        this as? JsonObject

    private fun JsonElement.asJsonArrayOrNull(): JsonArray? =
        this as? JsonArray
}
