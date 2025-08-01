package org.example.project.repository.dialogsRepository

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.contentType
import org.example.project.models.Dialog
import org.example.project.repository.levelRepository.LevelRepository

class KtorDialogsRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val levelsRepo: LevelRepository
)  {

    suspend fun getAllDialogs(): List<Dialog> =
        httpClient.get("$baseUrl/api/v1/dialogs").body()

    suspend fun getDialogById(id: Int): Dialog? =
        httpClient.get("$baseUrl/api/v1/dialogs/$id").body()

    suspend fun getDialogsByLevelId(levelId: Int): List<Dialog> =
        httpClient.get("$baseUrl/api/v1/dialogs/level/$levelId").body()


    suspend fun createDialog(dialog: Dialog, idLevel: Int): Dialog {
        return httpClient.post("$baseUrl/api/v1/dialogs/$idLevel") {
            println("KtorDialogsRepository: createDialog llamado con dialog=$dialog, levelId=$idLevel")

            contentType(io.ktor.http.ContentType.Application.Json)

            setBody(dialog)
            println("KtorDialogsRepository: Diálogo creado y retornado")
        }.body()
    }

    suspend fun updateDialog(id: Int, dialog: Dialog): Dialog {
        return httpClient.put("$baseUrl/api/v1/dialogs/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)

            setBody(dialog)
        }.body()
    }

    suspend fun deleteDialog(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/dialogs/$id")
        return response.status == io.ktor.http.HttpStatusCode.NoContent
    }

    suspend fun getAllLevelsFromDialogsRepo() = levelsRepo.getAllLevels()

}