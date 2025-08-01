package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.PhraseWordDto
import org.example.project.dtos.CreatePhraseWordDto

class PhraseWordRepository(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllPhraseWords(): List<PhraseWordDto> =
        httpClient.get("$baseUrl/api/v1/phraseWord").body()

    suspend fun getPhraseWordById(id: Int): PhraseWordDto =
        httpClient.get("$baseUrl/api/v1/phraseWord/$id").body()

    suspend fun getPhraseWordsByPhraseId(phraseId: Int): List<PhraseWordDto> =
        httpClient.get("$baseUrl/api/v1/phraseWord/phrase/$phraseId").body()

    suspend fun getPhraseWordsByWordId(wordId: Int): List<PhraseWordDto> =
        httpClient.get("$baseUrl/api/v1/phraseWord/word/$wordId").body()

    suspend fun createPhraseWord(dto: CreatePhraseWordDto): PhraseWordDto =
        httpClient.post("$baseUrl/api/v1/phraseWord") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updatePhraseWord(id: Int, dto: CreatePhraseWordDto): PhraseWordDto =
        httpClient.put("$baseUrl/api/v1/phraseWord/$id") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun deletePhraseWord(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/phraseWord/$id") {
            contentType(ContentType.Application.Json)
        }
        return response.status.value == 204
    }
}
