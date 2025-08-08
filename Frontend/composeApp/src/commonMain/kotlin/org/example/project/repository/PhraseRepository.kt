package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.PhraseDto
import org.example.project.dtos.CreatePhraseDto
import org.example.project.dtos.OrderPhraseDto

class PhraseRepository(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllPhrases(): List<PhraseDto> =
        httpClient.get("$baseUrl/api/v1/phrases").body()

    suspend fun getPhraseById(id: Int): PhraseDto =
        httpClient.get("$baseUrl/api/v1/phrases/$id").body()

    suspend fun getPhrasesByParticipantId(participantId: Int): List<PhraseDto> =
        httpClient.get("$baseUrl/api/v1/phrases/participant/$participantId").body()

    suspend fun createPhrase(participantId: Int, phrase: CreatePhraseDto): PhraseDto =
        httpClient.post("$baseUrl/api/v1/phrases/$participantId") {
            contentType(ContentType.Application.Json)
            setBody(phrase)
        }.body()

    suspend fun updatePhrase(id: Int, phrase: CreatePhraseDto): PhraseDto =
        httpClient.put("$baseUrl/api/v1/phrases/$id") {
            contentType(ContentType.Application.Json)
            setBody(phrase)
        }.body()

    suspend fun deletePhrase(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/phrases/$id") {
            contentType(ContentType.Application.Json)
        }
        return response.status.value == 204
    }

    suspend fun createPhraseOrder(order: OrderPhraseDto): OrderPhraseDto =
        httpClient.post("$baseUrl/api/v1/phrases/order") {
            contentType(ContentType.Application.Json)
            setBody(order)
        }.body()

    suspend fun orderPhrase(orderDto: OrderPhraseDto): Int? =
        httpClient.get("$baseUrl/api/v1/phrases/order") {
            contentType(ContentType.Application.Json)
            setBody(orderDto)
        }.body()

}
