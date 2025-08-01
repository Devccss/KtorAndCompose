package org.example.project.repository.UsersRepository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import org.example.project.models.Users

class UserRepo(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllUsers(): List<Users> =
        httpClient.get("$baseUrl/api/v1/users").body()

    suspend fun getUserById(id: Int): Users? =
        httpClient.get("$baseUrl/api/v1/users/$id").body()

    suspend fun getUserByEmail(email: String): Users? =
        httpClient.get("$baseUrl/api/v1/users/email/$email").body()

    suspend fun loginUser(email: String, password: String): Users? =
        httpClient.post("$baseUrl/api/v1/users/login") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }.body()
    suspend fun createUser(user: Users): Users =
        httpClient.post("$baseUrl/api/v1/users") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.body()

    suspend fun updateUser(id: Int, user: Users): Users =
        httpClient.post("$baseUrl/api/v1/users/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.body()

    suspend fun deleteUser(id: Int): Boolean {
        val response = httpClient.post("$baseUrl/api/v1/users/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
        }
        return response.status.value == 204
    }

}