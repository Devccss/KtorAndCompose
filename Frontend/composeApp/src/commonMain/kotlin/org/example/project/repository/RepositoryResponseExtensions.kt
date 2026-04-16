package org.example.project.repository

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ApiErrorDto(
    val message: String? = null,
    val error: String? = null
)

private val repositoryJson = Json {
    ignoreUnknownKeys = true
}

suspend inline fun <reified T> HttpResponse.parseOrThrow(): T {
    if (status.isSuccess()) return body()
    throw Exception(parseErrorMessage())
}

suspend fun HttpResponse.ensureSuccessOrThrow(): Boolean {
    if (status.isSuccess()) return true
    throw Exception(parseErrorMessage())
}

suspend fun HttpResponse.parseErrorMessage(): String {
    val raw = bodyAsText().trim()
    val parsed = runCatching {
        repositoryJson.decodeFromString(ApiErrorDto.serializer(), raw)
    }.getOrNull()

    val detail = when {
        !parsed?.message.isNullOrBlank() -> parsed.message
        !parsed?.error.isNullOrBlank() -> parsed.error
        raw.isNotBlank() -> raw.removePrefix("{").removeSuffix("}").trim().ifBlank {
            "Error HTTP ${status.value}"
        }
        else -> "Error HTTP ${status.value}"
    }

    return "error: $detail"
}

