package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
        httpClient.get("$baseUrl/api/v1/questions").body()

    suspend fun getQuestionById(id: Int): QuestionDto? =
        httpClient.get("$baseUrl/api/v1/questions/$id").body()

    suspend fun getQuestionsByExerciseId(exerciseId: Int): List<QuestionDto> =
        httpClient.get("$baseUrl/api/v1/questions/exercise/$exerciseId").body()

     suspend fun createQuestion(exerciseId: Int,question: CreateQuestionDto): QuestionDto =
        httpClient.post("$baseUrl/api/v1/questions/$exerciseId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(question)
        }.body()

     suspend fun updateQuestion(id: Int, question: UpdateQuestionDto): QuestionDto =
        httpClient.put("$baseUrl/api/v1/questions/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(question)
        }.body()

     suspend fun deleteQuestion(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/questions/$id").body()


    //Alternatives

    suspend fun getAlternativesByQuestionId(questionId: Int): List<AlternativesDto> =
        httpClient.get("$baseUrl/api/v1/alternatives/question/$questionId").body()

    suspend fun createAlternative(questionId: Int ,alternative: CreateAlternativeDto): AlternativesDto =
        httpClient.post("$baseUrl/api/v1/alternatives/$questionId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(alternative)
        }.body()

    suspend fun updateAlternative(id: Int, alternative: UpdateAlternativeDto): Boolean =
        httpClient.put("$baseUrl/api/v1/alternatives/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(alternative)
        }.body()

    suspend fun deleteAlternative(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/alternatives/$id").body()

}