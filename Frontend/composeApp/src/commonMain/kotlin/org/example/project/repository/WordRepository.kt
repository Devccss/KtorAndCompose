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
        httpClient.get("$baseUrl/api/v1/words").parseOrThrow()

    suspend fun searchWords(filterWordsDto: FilterWordsDto): List<WordDto> =
        httpClient.post("$baseUrl/api/v1/words/search") {
            contentType(ContentType.Application.Json)
            setBody(filterWordsDto)
        }.parseOrThrow()
    suspend fun getWordById(id: Int): WordDto =
        httpClient.get("$baseUrl/api/v1/words/$id").parseOrThrow()

    suspend fun createWord(word: CreateWordDto): WordDto =
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(word)
        }.parseOrThrow()

    suspend fun updateWord(id: Int, word: UpdateWordDto): Boolean =
        httpClient.put("$baseUrl/api/v1/words/$id") {
            contentType(ContentType.Application.Json)
            setBody(word)
        }.ensureSuccessOrThrow()

    suspend fun deleteWord(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/words/$id").ensureSuccessOrThrow()



    //ExerciseWord
    suspend fun getExerciseWordsByExerciseId(exerciseId: Int): List<ExerciseWordDto> =
        httpClient.get("$baseUrl/api/v1/exerciseWords/exercise/$exerciseId").parseOrThrow()

    suspend fun createExerciseWord(dto: CreateExerciseWordDto): ExerciseWordDto {
        return httpClient.post("$baseUrl/api/v1/exerciseWords") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
            .parseOrThrow()
    }


    suspend fun updateExerciseWord(idExerciseWord: Int,dto: UpdateExerciseWordDto): Boolean =
        httpClient.put("$baseUrl/api/v1/exerciseWords/$idExerciseWord") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.ensureSuccessOrThrow()

    suspend fun deleteExerciseWord(id: Int): Boolean {
        return httpClient.delete("$baseUrl/api/v1/exerciseWords/$id") {
            contentType(ContentType.Application.Json)
        }
            .ensureSuccessOrThrow()
    }
}
