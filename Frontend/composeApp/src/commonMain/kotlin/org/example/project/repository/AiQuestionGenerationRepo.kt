package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.ConfirmAiQuestionRequestDto
import org.example.project.dtos.ConfirmAiQuestionResponseDto
import org.example.project.dtos.GenerateQuestionsFromAiRequestDto
import org.example.project.dtos.GenerateQuestionsFromAiResponseDto

class AiQuestionGenerationRepo(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {

    suspend fun generateQuestions(
        contentId: Int,
        request: GenerateQuestionsFromAiRequestDto
    ): GenerateQuestionsFromAiResponseDto =
        httpClient.post("$baseUrl/api/v1/ai/questions/generate/$contentId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parseOrThrow()

    suspend fun confirmQuestion(
        contentId: Int,
        request: ConfirmAiQuestionRequestDto
    ): ConfirmAiQuestionResponseDto =
        httpClient.post("$baseUrl/api/v1/ai/questions/confirm/$contentId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parseOrThrow()
}

