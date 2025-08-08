package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.UsersDto

class StudentRepository(val httpClient: HttpClient, private val baseUrl: String) {
    suspend fun getStudentDetails(studentId: Int): UsersDto? =
        httpClient.get("$baseUrl/api/v1/student/$studentId").body()

    suspend fun getStudentProgress(studentId: Int): Any =
        httpClient.get("$baseUrl/api/v1/student/progress/${studentId}").body()

    suspend fun getStudentDialogs(studentId: Int): Any =
        httpClient.get("$baseUrl/api/v1/student/$studentId/dialogs").body()

    suspend fun getFullStudentDialog(dialogId: Int): Any =
        httpClient.get("$baseUrl/api/v1/student/dialog/$dialogId/full").body()

    suspend fun getStudentStandbyPhrases(studentId: Int): List<Any> =
        httpClient.get("$baseUrl/api/v1/student/$studentId/standby").body()

    suspend fun addPhraseToStandby(studentId: Int, phraseId: Int): Any =
        httpClient.get("$baseUrl/api/v1/student/$studentId/standby/add/$phraseId").body()

    suspend fun updateStandbyPhrase(standbyId: Int, updateDto: Any): Any =
        httpClient.get("$baseUrl/api/v1/student/standby/$standbyId/"){
            contentType(ContentType.Application.Json)
            setBody(updateDto)
        }.body()

    suspend fun deleteStandbyPhrase(standbyId: Int): Boolean {
        val delete = httpClient.get("$baseUrl/api/v1/student/standby/$standbyId")
        return delete.status == io.ktor.http.HttpStatusCode.NoContent
    }
}