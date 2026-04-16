package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.dtos.CreateTestCompletedDto
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateTestExerciseDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.TestCompletedDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.TestExerciseDto
import org.example.project.dtos.UpdateTestCompletedDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateTestExerciseDto

class TestRepo(private val httpClient: HttpClient ,private val baseUrl: String) {

    suspend fun getAllTests(): List<TestDto> =
        httpClient.get("$baseUrl/api/v1/tests").parseOrThrow()

    suspend fun getTestById(id: Int): TestDto? =
        httpClient.get("$baseUrl/api/v1/tests/$id").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getTestsByUnitId(unitId: Int): TestDto? =
        httpClient.get("$baseUrl/api/v1/tests/byUnit/$unitId").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getTestByExerciseId(exerciseId: Int): TestDto? =
        httpClient.get("$baseUrl/api/v1/tests/byExercise/$exerciseId").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getExercisesByTestId(testId: Int): List<ExerciseDto> =
        httpClient.get("$baseUrl/api/v1/tests/exercises/$testId").parseOrThrow()

    suspend fun createTest(test: CreateTestDto): TestDto =
        httpClient.post("$baseUrl/api/v1/tests") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(test)
        }.parseOrThrow()

    suspend fun updateTest(id: Int, test: UpdateTestDto): Boolean =
        httpClient.put("$baseUrl/api/v1/tests/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(test)
        }.ensureSuccessOrThrow()

    suspend fun deleteTest(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/tests/$id").ensureSuccessOrThrow()

    suspend fun searchTests(name: String?, unitId: Int?, isActive: Boolean?): List<TestDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/tests/search")
            name?.let { parameter("name", it) }
            unitId?.let { parameter("unitId", it) }
            isActive?.let { parameter("isActive", it) }
        }.parseOrThrow()
    }


    //Test-Exersice
    suspend fun getAllTestExercises(): List<TestExerciseDto> =
        httpClient.get("$baseUrl/api/v1/testExercises").parseOrThrow()

    suspend fun createTestExercise(dto: CreateTestExerciseDto): TestExerciseDto =
        httpClient.post("$baseUrl/api/v1/testExercises") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()


    suspend fun updateTestExercise(id: Int, dto: UpdateTestExerciseDto): Boolean =
        httpClient.put("$baseUrl/api/v1/testExercises/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.ensureSuccessOrThrow()

    suspend fun deleteTestExercise(exerciseId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/testExercises/$exerciseId").ensureSuccessOrThrow()



    //CompleteTest
    suspend fun getAllTestCompleted(): List<TestCompletedDto> =
        httpClient.get("$baseUrl/api/v1/testsCompleted").parseOrThrow()


    suspend fun getTestCompletedById(id: Int): TestCompletedDto? =
        httpClient.get("$baseUrl/api/v1/testsCompleted/$id").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getTestsCompletedByUser(userId: Int): List<TestCompletedDto> =
        httpClient.get("$baseUrl/api/v1/testsCompleted/user/$userId").parseOrThrow()


    suspend fun createTestCompleted(dto: CreateTestCompletedDto): TestCompletedDto =
        httpClient.post("$baseUrl/api/v1/testsCompleted") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto): Boolean =
        httpClient.put("$baseUrl/api/v1/testsCompleted/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.ensureSuccessOrThrow()

    suspend fun deleteTestCompleted(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/testsCompleted/$id").ensureSuccessOrThrow()
}