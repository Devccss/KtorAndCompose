package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.example.project.dtos.LearningDashboardDto

class LearningDashboardRepository(
    private val httpClient: HttpClient, private val baseUrl: String
) {

    suspend fun getDashboard(): LearningDashboardDto =
         httpClient.get("$baseUrl/api/v1/learningDashboard"){
             addAuthHeader()
         }.parseOrThrow()

}