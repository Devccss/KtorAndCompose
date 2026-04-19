package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUsersDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.UserDto

class UserRepo(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllUsers(): List<UserDto> =
        httpClient.get("$baseUrl/api/v1/users").parseOrThrow()


    suspend fun getUserById(id: Int): UserDto? =
        httpClient.get("$baseUrl/api/v1/users/$id").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getUserByEmail(email: String): UserDto? =
        httpClient.get("$baseUrl/api/v1/users/email/$email").let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun searchUsers(filters: FilterUsersDto): List<UserDto> =
        httpClient.get {
            url("$baseUrl/api/v1/users/search")
            filters.name?.let { parameter("name", it) }
            filters.unitId?.let { parameter("unitId", it) }
            filters.role?.let { parameter("role", it.name) }
        }.parseOrThrow()

    suspend fun getFilterUsers(filters: FilterUsersDto): List<UserDto> =
        searchUsers(filters)

    suspend fun getUsersByName(name: String): List<UserDto> =
        httpClient.get("$baseUrl/api/v1/users/name/$name").parseOrThrow()

    suspend fun loginUser(dto: LoginDto): UserDto =
        httpClient.post("$baseUrl/api/v1/users/login") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun createUser(user: CreateUserDto): UserDto =
        httpClient.post("$baseUrl/api/v1/users/register") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.parseOrThrow()

    suspend fun updateUser(id: Int, user: UserDto): Boolean =
        httpClient.put("$baseUrl/api/v1/users/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.ensureSuccessOrThrow()

    suspend fun deleteUser(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/users/$id").ensureSuccessOrThrow()


}