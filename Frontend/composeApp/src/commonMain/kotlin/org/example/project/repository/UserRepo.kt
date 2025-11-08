package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.UsersDto
import org.example.project.models.Users

class UserRepo(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllUsers(): List<Users> =
        httpClient.get("$baseUrl/api/v1/users").body()


    suspend fun getUserById(id: Int): Users? =
        httpClient.get("$baseUrl/api/v1/users/$id").body()

    suspend fun getUserByEmail(email: String): UsersDto? =
        httpClient.get("$baseUrl/api/v1/users/email/$email").body()


    suspend fun loginUser(dto: LoginDto): UsersDto =
        httpClient.post("$baseUrl/api/v1/users/login") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun createUser(user: CreateUserDto): Users = try {
        httpClient.post("$baseUrl/api/v1/users/register") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.body()
    } catch (e: Exception) {
        throw e
    }

    suspend fun updateUser(id: Int, user: Users): UsersDto =
        httpClient.put("$baseUrl/api/v1/users/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.body()

    suspend fun deleteUser(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/users/$id").body()


}