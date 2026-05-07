package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.CreateExerciseWordDto
import org.example.project.dtos.CreateWordDto
import org.example.project.dtos.ExerciseWordDto
import org.example.project.dtos.FilterWordsDto
import org.example.project.dtos.WordDto
import org.example.project.dtos.UpdateExerciseWordDto
import org.example.project.dtos.UpdateWordDto

class WordRepository(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun getAllWords(): List<WordDto> =
        httpClient.get("$baseUrl/api/v1/words") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun searchWords(filterWordsDto: FilterWordsDto): List<WordDto> =
        httpClient.get {
            url("$baseUrl/api/v1/words/search")
            filterWordsDto.exerciseId?.let { parameter("exerciseId", it) }
            filterWordsDto.spanish?.let { parameter("spanish", it) }
            filterWordsDto.english?.let { parameter("english", it) }
            filterWordsDto.phonetic?.let { parameter("phonetic", it) }
            filterWordsDto.isActive?.let { parameter("isActive", it) }
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getWordById(id: Int): WordDto =
        httpClient.get("$baseUrl/api/v1/words/$id") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun createWord(word: CreateWordDto): WordDto =
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(word)
            addAuthHeader()
        }.parseOrThrow()

    suspend fun updateWord(id: Int, word: UpdateWordDto): Boolean =
        httpClient.put("$baseUrl/api/v1/words/$id") {
            contentType(ContentType.Application.Json)
            setBody(word)
            addAuthHeader()
        }.ensureSuccessOrThrow()

    suspend fun deleteWord(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/words/$id") {
            addAuthHeader()
        }.ensureSuccessOrThrow()

    //ExerciseWord
    suspend fun getExerciseWordsByExerciseId(exerciseId: Int): List<ExerciseWordDto> =
        httpClient.get("$baseUrl/api/v1/exerciseWords/exercise/$exerciseId") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun createExerciseWord(dto: CreateExerciseWordDto): ExerciseWordDto {
        return httpClient.post("$baseUrl/api/v1/exerciseWords") {
            contentType(ContentType.Application.Json)
            setBody(dto)
            addAuthHeader()
        }
            .parseOrThrow()
    }

    suspend fun updateExerciseWord(idExerciseWord: Int, dto: UpdateExerciseWordDto): Boolean =
        httpClient.put("$baseUrl/api/v1/exerciseWords/$idExerciseWord") {
            contentType(ContentType.Application.Json)
            setBody(dto)
            addAuthHeader()
        }.ensureSuccessOrThrow()

    suspend fun deleteExerciseWord(id: Int): Boolean {
        return httpClient.delete("$baseUrl/api/v1/exerciseWords/$id") {
            contentType(ContentType.Application.Json)
            addAuthHeader()
        }
            .ensureSuccessOrThrow()
    }
}
