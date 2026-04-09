package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.example.project.dtos.CreateExerciseContentDto
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.CreateExerciseCompletedDto
import org.example.project.dtos.ExerciseCompletedDto
import org.example.project.dtos.ExerciseContentDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.UpdateExerciseCompletedDto
import org.example.project.dtos.UpdateExerciseContentDto
import org.example.project.dtos.UpdateExerciseDto

class ExerciseRepo(private val httpClient: HttpClient, private val baseUrl: String) {

    suspend fun searchExercises(filters: FilterExercisesDto): List<ExerciseDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/exercises/search")
            filters.name?.let { parameter("name", it) }
            filters.isActive?.let { parameter("isActive", it) }
        }.body()
    }

    suspend fun getAllExercises(): List<ExerciseDto> =
        httpClient.get("$baseUrl/api/v1/exercises").body()

    suspend fun getExerciseById(id: Int): ExerciseDto? =
        httpClient.get("$baseUrl/api/v1/exercises/$id").body()

    suspend fun getExercisesByUnitId(unitId: Int): List<ExerciseDto> =
        httpClient.get("$baseUrl/api/v1/exercises/unit/$unitId").body()

    suspend fun createExercise(exercise: CreateExerciseDto): ExerciseDto =
        httpClient.post("$baseUrl/api/v1/exercises") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(exercise)
        }.body()

    suspend fun updateExercise(id: Int, exercise: UpdateExerciseDto): Boolean =
        httpClient.put("$baseUrl/api/v1/exercises/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(exercise)
        }.status.isSuccess()

    suspend fun reorderExercises(orders: List<Pair<Int, Int>>): Boolean =
        httpClient.put("$baseUrl/api/v1/exercises/reorder") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(orders)
        }.status.isSuccess()


    suspend fun deleteExercise(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/exercises/$id").body()


    // ExerciseContent

    suspend fun getAllExerciseContent(): List<ExerciseContentDto> =
        httpClient.get("$baseUrl/api/v1/exerciseContent").body()

    suspend fun getExerciseContentByExerciseId(exerciseId: Int): ExerciseContentDto? {
        val response = httpClient.get("$baseUrl/api/v1/exerciseContent/exercise/$exerciseId")
        return if (response.status == io.ktor.http.HttpStatusCode.NotFound) null else response.body()
    }

    suspend fun createExerciseContent(
        exerciseId: Int,
        content: CreateExerciseContentDto
    ): ExerciseContentDto =
        httpClient.post("$baseUrl/api/v1/exerciseContent/$exerciseId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(content)
        }.body()

    suspend fun updateExerciseContent(exerciseId: Int, content: UpdateExerciseContentDto): Boolean =
        httpClient.put("$baseUrl/api/v1/exerciseContent/exercise/$exerciseId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(content)
        }.status.isSuccess()

    suspend fun deleteExerciseContent(exerciseId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/exerciseContent/exercise/$exerciseId").body()


    //ExercisesCompleted
    suspend fun getAllExercisesCompleted(): List<ExerciseCompletedDto> =
        httpClient.get("$baseUrl/api/v1/exercisesCompleted").body()

    suspend fun getExercisesCompletedByUserId(userId: Int): List<ExerciseCompletedDto> =
        httpClient.get("$baseUrl/api/v1/exercisesCompleted/user/$userId").body()

    suspend fun createExerciseCompleted(dto: CreateExerciseCompletedDto): ExerciseCompletedDto =
        httpClient.post("$baseUrl/api/v1/exercisesCompleted") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updateExerciseCompleted(id: Int, dto: UpdateExerciseCompletedDto): Boolean =
        httpClient.put("$baseUrl/api/v1/exercisesCompleted/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.status.isSuccess()

    suspend fun deleteExerciseCompleted(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/exercisesCompleted/$id").body()
}