package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.dtos.CloseUserSessionLogDto
import org.example.project.dtos.CreateUserSessionLogDto
import org.example.project.dtos.UserSessionLogDto
import org.example.project.dtos.WeeklySessionMetricDto

class SessionLogRepo(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun startSession(dto: CreateUserSessionLogDto): UserSessionLogDto =
        httpClient.post("$baseUrl/api/v1/session-logs/start") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun closeSessionById(id: Int, dto: CloseUserSessionLogDto): UserSessionLogDto =
        httpClient.put("$baseUrl/api/v1/session-logs/$id/close") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun closeOpenSessionByUserId(userId: Int, dto: CloseUserSessionLogDto): UserSessionLogDto? {
        val response = httpClient.put("$baseUrl/api/v1/session-logs/user/$userId/close-open") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
        return if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
    }

    suspend fun getOpenSessionByUserId(userId: Int): UserSessionLogDto? {
        val response = httpClient.get("$baseUrl/api/v1/session-logs/user/$userId/open")
        return if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
    }

    suspend fun getSessionsByUserId(userId: Int): List<UserSessionLogDto> =
        httpClient.get("$baseUrl/api/v1/session-logs/user/$userId").parseOrThrow()

    suspend fun getWeeklyMetrics(
        fromDate: String? = null,
        toDate: String? = null
    ): List<WeeklySessionMetricDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/session-logs/metrics/weekly")
            fromDate?.let { parameter("fromDate", it) }
            toDate?.let { parameter("toDate", it) }
        }.parseOrThrow()
    }
}

