package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

@Serializable
private data class ApiErrorDto(
    val message: String? = null,
    val error: String? = null
)

class TestRepo(private val httpClient: HttpClient ,private val baseUrl: String) {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend inline fun <reified T> parseOrThrow(response: HttpResponse): T {
        if (response.status.isSuccess()) return response.body()
        throw Exception(parseError(response))
    }

    private suspend fun ensureSuccessOrThrow(response: HttpResponse): Boolean {
        if (response.status.isSuccess()) return true
        throw Exception(parseError(response))
    }

    private suspend fun parseError(response: HttpResponse): String {
        val raw = response.bodyAsText().trim()
        val parsed = runCatching {
            json.decodeFromString(ApiErrorDto.serializer(), raw)
        }.getOrNull()

        val detail = when {
            !parsed?.message.isNullOrBlank() -> parsed.message
            !parsed?.error.isNullOrBlank() -> parsed.error
            raw.isNotBlank() -> raw.removePrefix("{").removeSuffix("}").trim().ifBlank {
                "Error HTTP ${response.status.value}"
            }
            else -> "Error HTTP ${response.status.value}"
        }

        return "error: $detail"
    }

    suspend fun getAllTests(): List<TestDto> =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/tests"))

    suspend fun getTestById(id: Int): TestDto? =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/tests/$id"))

    suspend fun getTestsByUnitId(unitId: Int): TestDto? =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/tests/byUnit/$unitId"))

    suspend fun getTestByExerciseId(exerciseId: Int): TestDto? =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/tests/byExercise/$exerciseId"))

    suspend fun getExercisesByTestId(testId: Int): List<ExerciseDto> =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/tests/exercises/$testId"))

    suspend fun createTest(test: CreateTestDto): TestDto =
        parseOrThrow(
            httpClient.post("$baseUrl/api/v1/tests") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(test)
            }
        )

    suspend fun updateTest(id: Int, test: UpdateTestDto): Boolean =
        ensureSuccessOrThrow(
            httpClient.put("$baseUrl/api/v1/tests/$id") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(test)
            }
        )

    suspend fun deleteTest(id: Int): Boolean =
        parseOrThrow(httpClient.delete("$baseUrl/api/v1/tests/$id"))

    //Test-Exersice
    suspend fun getAllTestExercises(): List<TestExerciseDto> =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/testExercises"))

    suspend fun createTestExercise(dto: CreateTestExerciseDto): TestExerciseDto =
        parseOrThrow(
            httpClient.post("$baseUrl/api/v1/testExercises") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(dto)
            }
        )


    suspend fun updateTestExercise(id: Int, dto: UpdateTestExerciseDto): Boolean =
        ensureSuccessOrThrow(
            httpClient.put("$baseUrl/api/v1/testExercises/$id") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(dto)
            }
        )

    suspend fun deleteTestExercise(exerciseId: Int): Boolean =
        parseOrThrow(httpClient.delete("$baseUrl/api/v1/testExercises/$exerciseId"))



    //CompleteTest
    suspend fun getAllTestCompleted(): List<TestCompletedDto> =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/testsCompleted"))


    suspend fun getTestCompletedById(id: Int): TestCompletedDto? =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/testsCompleted/$id"))

    suspend fun getTestsCompletedByUser(userId: Int): List<TestCompletedDto> =
        parseOrThrow(httpClient.get("$baseUrl/api/v1/testsCompleted/user/$userId"))


    suspend fun createTestCompleted(dto: CreateTestCompletedDto): TestCompletedDto =
        parseOrThrow(
            httpClient.post("$baseUrl/api/v1/testsCompleted") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(dto)
            }
        )

    suspend fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto): Boolean =
        ensureSuccessOrThrow(
            httpClient.put("$baseUrl/api/v1/testsCompleted/$id") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(dto)
            }
        )

    suspend fun deleteTestCompleted(id: Int): Boolean =
        parseOrThrow(httpClient.delete("$baseUrl/api/v1/testsCompleted/$id"))
}