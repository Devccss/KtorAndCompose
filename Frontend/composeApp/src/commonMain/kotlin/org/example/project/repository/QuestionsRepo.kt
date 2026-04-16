package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import org.example.project.dtos.AlternativesDto
import org.example.project.dtos.CreateAlternativeDto
import org.example.project.dtos.CreateQuestionDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateQuestionDto

class QuestionsRepo(private val httpClient: HttpClient, private val baseUrl: String) {

 suspend fun getAllQuestions(): List<QuestionDto> =
        httpClient.get("$baseUrl/api/v1/questions").parseOrThrow()

    suspend fun getQuestionById(id: Int): QuestionDto? =
        httpClient.get("$baseUrl/api/v1/questions/$id").parseOrThrow()

    suspend fun getQuestionsByExerciseId(contentId: Int): List<QuestionDto> =
        httpClient.get("$baseUrl/api/v1/questions/exercise/$contentId").parseOrThrow()

     suspend fun createQuestion(contentId: Int,question: CreateQuestionDto): QuestionDto =
        httpClient.post("$baseUrl/api/v1/questions/$contentId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(question)
        }.parseOrThrow()

     suspend fun updateQuestion(id: Int, question: UpdateQuestionDto): Boolean =
        httpClient.put("$baseUrl/api/v1/questions/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(question)
        }.ensureSuccessOrThrow()

     suspend fun deleteQuestion(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/questions/$id").ensureSuccessOrThrow()


    //Alternatives

    suspend fun getAlternativesByQuestionId(questionId: Int): List<AlternativesDto> =
        httpClient.get("$baseUrl/api/v1/alternatives/question/$questionId").parseOrThrow()

    suspend fun createAlternative(questionId: Int ,alternative: CreateAlternativeDto): AlternativesDto =
        httpClient.post("$baseUrl/api/v1/alternatives/$questionId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(alternative)
        }.parseOrThrow()

    suspend fun updateAlternative(id: Int, alternative: UpdateAlternativeDto): Boolean =
        httpClient.put("$baseUrl/api/v1/alternatives/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(alternative)
        }.ensureSuccessOrThrow()

    suspend fun deleteAlternative(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/alternatives/$id").ensureSuccessOrThrow()

}