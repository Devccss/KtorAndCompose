package org.example.project.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.dtos.CreateNotificationDto
import org.example.project.dtos.FilterNotificationsDto
import org.example.project.dtos.NotificationDto
import org.example.project.dtos.UpdateNotificationDto

class NotificationRepo(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {

    suspend fun getAllNotifications(): List<NotificationDto> =
        httpClient.get("$baseUrl/api/v1/notifications").parseOrThrow()

    suspend fun getNotificationsByUser(userId: Int): List<NotificationDto> =
        searchNotifications(FilterNotificationsDto(userId = userId))

    suspend fun getUnreadNotificationsByUser(userId: Int): List<NotificationDto> =
        searchNotifications(
            FilterNotificationsDto(
                userId = userId,
                status = org.example.project.dtos.NotificationStatus.UNREAD
            )
        )

    suspend fun searchNotifications(filters: FilterNotificationsDto): List<NotificationDto> {
        return httpClient.get {
            url("$baseUrl/api/v1/notifications/search")
            filters.userId?.let { parameter("userId", it) }
            filters.title?.let { parameter("title", it) }
            filters.notificationType?.let { parameter("notificationType", it.name) }
            filters.category?.let { parameter("category", it.name) }
            filters.subCategory?.let { parameter("subCategory", it.name) }
            filters.status?.let { parameter("status", it.name) }
        }.parseOrThrow()
    }

    suspend fun getNotificationById(id: Int): NotificationDto =
        httpClient.get("$baseUrl/api/v1/notifications/$id").parseOrThrow()

    suspend fun createNotification(dto: CreateNotificationDto): NotificationDto =
        httpClient.post("$baseUrl/api/v1/notifications") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.parseOrThrow()

    suspend fun updateNotification(id: Int, dto: UpdateNotificationDto): Boolean =
        httpClient.put("$baseUrl/api/v1/notifications/$id") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.ensureSuccessOrThrow()

    suspend fun markAsRead(id: Int): NotificationDto =
        httpClient.put("$baseUrl/api/v1/notifications/$id/read").parseOrThrow()

    suspend fun markAsUnread(id: Int): NotificationDto =
        httpClient.put("$baseUrl/api/v1/notifications/$id/unread").parseOrThrow()

    suspend fun deleteNotification(id: Int): Boolean =
        httpClient.delete("$baseUrl/api/v1/notifications/$id").ensureSuccessOrThrow()
}

