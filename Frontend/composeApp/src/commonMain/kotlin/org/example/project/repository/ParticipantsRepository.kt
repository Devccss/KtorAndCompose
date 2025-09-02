package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import org.example.project.dtos.CreateParticipantDTO
import org.example.project.dtos.DialogParticipantDTO

class ParticipantsRepository(private val httpClient: HttpClient,private val baseUrl: String){
    suspend fun getParticipantById(participantId: Int): DialogParticipantDTO =
        httpClient.get("$baseUrl/api/v1/participants/byId/$participantId").body()

    suspend fun getParticipantsByDialogId(dialogId: Int): List<DialogParticipantDTO> =
        httpClient.get("$baseUrl/api/v1/participants/$dialogId").body()

    suspend fun createParticipant(dialogId: Int,participant:CreateParticipantDTO): DialogParticipantDTO =
        httpClient.post("$baseUrl/api/v1/participants/$dialogId") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(participant)
        }.body()

    suspend fun updateParticipant(id: Int, participant: CreateParticipantDTO): Int =
        httpClient.put("$baseUrl/api/v1/participants/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(participant)

        }.body()


    suspend fun deleteParticipant(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/api/v1/participants/$id") {
            contentType(io.ktor.http.ContentType.Application.Json)
        }
        return response.status.value == 204
    }
}