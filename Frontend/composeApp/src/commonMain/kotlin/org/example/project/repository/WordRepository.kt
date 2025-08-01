package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.WordDto
import org.example.project.dtos.CreateWordDto

class WordRepository(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllWords(): List<WordDto> =
        httpClient.get("$baseUrl/api/v1/words").body()

    suspend fun getWordById(id: Int): WordDto =
        httpClient.get("$baseUrl/api/v1/words/$id").body()

    suspend fun getWordsByPhraseId(phraseId: Int): List<WordDto> =
        httpClient.get("$baseUrl/api/v1/words/phrase/$phraseId").body()

    suspend fun createWord(word: CreateWordDto): WordDto =
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(word)
        }.body()

    suspend fun updateWord(id: Int, word: CreateWordDto): WordDto =
        httpClient.put("$baseUrl/api/v1/words/$id") {
            contentType(ContentType.Application.Json)
            setBody(word)
        }.body()

    suspend fun deleteWord(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/words/$id") {
            contentType(ContentType.Application.Json)
        }
        return response.status.value == 204
    }
}
