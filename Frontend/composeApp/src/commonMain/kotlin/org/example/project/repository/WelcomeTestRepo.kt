package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateWelcomeTestDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UpdateWelcomeTestDto
import org.example.project.dtos.WelcomeTestDto

class WelcomeTestRepo(private val httpClient: HttpClient ,private val baseUrl: String) {
    suspend fun getAllWelcomeTests(): List<WelcomeTestDto> =
        httpClient.get("$baseUrl/api/v1/welcomeTest").parseOrThrow()

    suspend fun getAllTestsFromWelcomeTests(): List<TestDto> =
        httpClient.get("$baseUrl/api/v1/welcomeTest/tests").parseOrThrow()

    suspend fun getWelcomeTestById(id: Int): TestDto? =
        httpClient.get("$baseUrl/api/v1/welcomeTest/$id").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun createWelcomeTest(dto: CreateWelcomeTestDto): TestDto =
        httpClient.post("$baseUrl/api/v1/welcomeTest") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun createTestForWelcomeTest( dto: CreateTestDto): TestDto =
        httpClient.post("$baseUrl/api/v1/tests/welcome") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun updateWelcomeTest(testId: Int, dto: UpdateWelcomeTestDto): Boolean =
        httpClient.put("$baseUrl/api/v1/welcomeTest/$testId") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.ensureSuccessOrThrow()

    suspend fun deleteWelcomeTest(testId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/welcomeTest/$testId").ensureSuccessOrThrow()
}