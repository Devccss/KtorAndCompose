package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.UpdateExerciseDto

class ExerciseRepo(private val httpClient: HttpClient, private val baseUrl: String) {

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

    suspend fun updateExercise(id: Int, exercise: UpdateExerciseDto): ExerciseDto =
        httpClient.put("$baseUrl/api/v1/exercises/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(exercise)
        }.body()

    suspend fun deleteExercise(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/exercises/$id").body()
}