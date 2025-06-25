package org.example.project.network


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.models.Level
import org.example.project.repository.LevelRepository

class KtorLevelRepository(private val httpClient: HttpClient, private val baseUrl: String) : LevelRepository {

    override suspend fun getAllLevels(): List<Level> =
        httpClient.get("$baseUrl/api/v1/levels").body()

    override suspend fun getLevelById(id: Int): Level? =
        httpClient.get("$baseUrl/api/v1/levels/$id").body()

    override suspend fun addLevel(level: Level): Level =
        httpClient.post("$baseUrl/api/v1/levels") {
            contentType(ContentType.Application.Json)
            setBody(level)
        }.body()

    override suspend fun updateLevel(id: Int, level: Level): Level =
        httpClient.put("$baseUrl/api/v1/levels/$id") {
            contentType(ContentType.Application.Json)
            setBody(level)
        }.body()

    override suspend fun deleteLevel(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/levels/$id")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun getLevelsByDifficulty(difficulty: String): List<Level> =
        httpClient.get("$baseUrl/api/v1/levels/difficulty/$difficulty").body()
}
