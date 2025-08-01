package org.example.project.repository.UsersRepository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.example.project.models.Users

class UserRepo(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllUsers(): List<Users> {
        val response: HttpResponse = httpClient.get("$baseUrl/api/v1/users")
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            // Puedes lanzar una excepción o retornar una lista vacía
            return emptyList()
        }
    }

    suspend fun getUserById(id: Int): Users? =
        httpClient.get("$baseUrl/api/v1/users/$id").body()

    suspend fun getUserByEmail(email: String): Users? =
        httpClient.get("$baseUrl/api/v1/users/email/$email").body()


    suspend fun loginUser(email: String, password: String): Users? {
        val response: HttpResponse = httpClient.post("$baseUrl/api/v1/users/login") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            null
        }
    }
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