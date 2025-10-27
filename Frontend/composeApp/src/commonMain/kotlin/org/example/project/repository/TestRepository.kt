package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import org.example.project.dtos.CreateTest
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.models.Test

class TestRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val levelRepository: KtorLevelRepository,
    private val dialogsRepository: DialogsRepository
) {
    suspend fun createTest(test: CreateTest, levelId: Int): Test {
        return try {
            httpClient.post("$baseUrl/api/v1/test/$levelId") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(test)
            }.body()

        } catch (error: Exception) {
            println("Error creating test: $error")
            throw error
        }
    }

    suspend fun addDialogTest(dialogId: Int, testId: Int): Boolean =
        httpClient.post("$baseUrl/api/v1/test/addDialog/$dialogId/$testId") {
            contentType(io.ktor.http.ContentType.Application.Json)
        }.body()

    suspend fun getTestById(testId: Int): Test? =
        httpClient.get("$baseUrl/api/v1/test/$testId").body()

    suspend fun getAllTests(): List<Test> =
        httpClient.get("$baseUrl/api/v1/test").body()

    suspend fun editTest(test: CreateTest, testId: Int): Boolean =
        httpClient.put("$baseUrl/api/v1/test/$testId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(test)
        }.body()

    suspend fun deleteTest(testId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/test/$testId").body()


    suspend fun deleteDialogTest(dialogId: Int,testId: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/test/removeDialogTest/$dialogId/$testId").body()


    suspend fun getAllLevelsFromTestRepo():List<Level> = levelRepository.getAllLevels()

    suspend fun getLevelByIdFromTestRepo(levelId: Int):Level? = levelRepository.getLevelById(levelId)

    suspend fun getAllDialogsFromTestRepo():List<Dialog> = dialogsRepository.getAllDialogs()

    suspend fun getAllTestDialogs(testId: Int):List<Dialog> = dialogsRepository.getAllTestDialogs(testId)
}