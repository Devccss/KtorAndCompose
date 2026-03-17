package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateTestExerciseDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.TestExerciseDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateTestExerciseDto

class TestRepo(private val httpClient: HttpClient ,private val baseUrl: String) {
    suspend fun getAllTests(): List<TestDto> =
        httpClient.get("$baseUrl/api/v1/tests").body()

    suspend fun getTestById(id: Int): TestDto? =
        httpClient.get("$baseUrl/api/v1/tests/$id").body()

    suspend fun getExercisesByTestId(testId: Int): List<ExerciseDto> =
        httpClient.get("$baseUrl/api/v1/tests/exercises/$testId").body()

    suspend fun createTest(test: CreateTestDto): TestDto =
        httpClient.post("$baseUrl/api/v1/tests") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(test)
        }.body()

    suspend fun updateTest(id: Int, test: UpdateTestDto): Boolean =
        httpClient.put("$baseUrl/api/v1/tests/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(test)
        }.status.isSuccess()

     suspend fun deleteTest(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/tests/delete/$id").body()


    //Test-Exersice
    suspend fun getAllTestExercises(): List<TestExerciseDto> =
        httpClient.get("$baseUrl/api/v1/testExercises").body()


    suspend fun createTestExercise(dto:CreateTestExerciseDto): TestExerciseDto =
        httpClient.post("$baseUrl/api/v1/testExercises") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.body()

     suspend fun updateTestExercise(id: Int,dto: UpdateTestExerciseDto): Boolean =
        httpClient.put("$baseUrl/api/v1/testExercises/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.status.isSuccess()

     suspend fun deleteTestExercise(exerciseId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/testExercises/$exerciseId").body()
}