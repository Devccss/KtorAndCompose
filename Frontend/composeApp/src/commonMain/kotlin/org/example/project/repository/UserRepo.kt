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
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUsersDto
import org.example.project.dtos.GeneralStatsDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UpdateUserDto
import org.example.project.dtos.UserDto
import org.example.project.dtos.UserStatsDto
import org.example.project.dtos.UserWeeklyHoursDto
import org.example.project.dtos.StudentsStatsSummaryDto
import org.example.project.dtos.UnitDto

class UserRepo(private val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getAllUsers(): List<UserDto> {
        val url = "$baseUrl/api/v1/users"
        try {
            val result: List<UserDto> = httpClient.get(url) {
                addAuthHeader()
            }.parseOrThrow()
            return result
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserById(id: Int): UserDto? {
        val url = "$baseUrl/api/v1/users/$id"
        try {
            val response = httpClient.get(url) {
                addAuthHeader()
            }
            return if (response.status == HttpStatusCode.NotFound) {
                null
            } else {
                val result: UserDto = response.parseOrThrow()
                result
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserByEmail(email: String): UserDto? =
        httpClient.get("$baseUrl/api/v1/users/email/$email") {
            addAuthHeader()
        }.let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getAllUserStats(): GeneralStatsDto =
        httpClient.get("$baseUrl/api/v1/users/stats") {
            addAuthHeader()
        }.parseOrThrow()
    suspend fun getUserStats(userId: Int): UserStatsDto? =
        httpClient.get("$baseUrl/api/v1/users/$userId/stats") {
            addAuthHeader()
        }.let { response ->
            if (response.status == HttpStatusCode.NotFound) null else response.parseOrThrow()
        }

    suspend fun getCompletedUnitsByUserId(userId: Int): List<UnitDto> =
        httpClient.get("$baseUrl/api/v1/users/$userId/completed-units") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getCompletedExercisesByUserId(userId: Int): List<ExerciseDto> =
        httpClient.get("$baseUrl/api/v1/users/$userId/completed-exercises") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getCompletedTestsByUserId(userId: Int): List<TestDto> =
        httpClient.get("$baseUrl/api/v1/users/$userId/completed-tests") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getFailedTestsByUserId(userId: Int, minScore: Int = 60): List<TestDto> =
        httpClient.get("$baseUrl/api/v1/users/$userId/failed-tests") {
            parameter("minScore", minScore)
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getWeeklyHoursByUserId(userId: Int): UserWeeklyHoursDto =
        httpClient.get("$baseUrl/api/v1/users/$userId/weekly-hours") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getStudentsStats(): StudentsStatsSummaryDto =
        httpClient.get("$baseUrl/api/v1/users/stats/students") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun searchUsers(filters: FilterUsersDto): List<UserDto> =
        httpClient.get {
            url("$baseUrl/api/v1/users/filter")
            filters.name?.let { parameter("name", it) }
            filters.unitId?.let { parameter("unitId", it) }
            filters.role?.let { parameter("role", it.name) }
            addAuthHeader()
        }.parseOrThrow()

    suspend fun getFilterUsers(filters: FilterUsersDto): List<UserDto> =
        searchUsers(filters)

    suspend fun getUsersByName(name: String): List<UserDto> =
        httpClient.get("$baseUrl/api/v1/users/name/$name") {
            addAuthHeader()
        }.parseOrThrow()

    suspend fun loginUser(dto: LoginDto): UserDto {
        val url = "$baseUrl/api/v1/users/login"
        try {
            val response = httpClient.post(url) {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(dto)
            }
            val result: UserDto = response.parseOrThrow()
            return result
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createUser(user: CreateUserDto): UserDto =
        httpClient.post("$baseUrl/api/v1/users/register") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
        }.parseOrThrow()

    suspend fun updateUser(id: Int, user: UpdateUserDto): Boolean =
        httpClient.put("$baseUrl/api/v1/users/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(user)
            addAuthHeader()
        }.ensureSuccessOrThrow()

    suspend fun deleteUser(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/users/$id") {
            addAuthHeader()
        }.ensureSuccessOrThrow()
}