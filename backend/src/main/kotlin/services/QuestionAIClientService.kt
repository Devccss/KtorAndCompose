package com.example.services

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.BadRequestException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.Throws


class QuestionAIClientService(
    private val baseUrl: String,
    private val apiKey: String?,
    timeoutMs: Long
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) { json(jsonParser) }
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMs
            connectTimeoutMillis = timeoutMs
            socketTimeoutMillis = timeoutMs
        }
    }

    suspend fun generateQuestions(requestPayload: JsonObject): JsonObject {
        val rawResponse = client.post("$baseUrl/v1/questions/generate") {
            contentType(ContentType.Application.Json)
            if (!apiKey.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            setBody(requestPayload)
        }
        val success = rawResponse.status.isSuccess()
        if (!success) throw BadRequestException("Error o IA no disponible")



        return jsonParser.decodeFromString(JsonObject.serializer(), rawResponse.bodyAsText())
    }
}
