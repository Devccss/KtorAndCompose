package org.example.project.repository


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.models.Level

abstract class KtorLevelRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : LevelRepository {

    override suspend fun addLevel(level: Level): Level =
        httpClient.post("$baseUrl/levels") {
            contentType(ContentType.Application.Json)
            setBody(level)
        }.body()

    override suspend fun getAllLevels(): List<Level> =
        httpClient.get("$baseUrl/levels").body()

    override suspend fun updateLevel(id: Int, level: Level): Level =
        httpClient.put("$baseUrl/levels/$id") {
            contentType(ContentType.Application.Json)
            setBody(level)
        }.body()

    override suspend fun deleteLevel(id: Int): Boolean {
        val res = httpClient.delete("$baseUrl/levels/$id")
        return res.status == HttpStatusCode.NoContent
    }
}
