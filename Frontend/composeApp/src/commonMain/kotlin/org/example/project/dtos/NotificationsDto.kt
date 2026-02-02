package org.example.project.dtos

import kotlinx.serialization.Serializable

enum class NotificationType { INFO, WARNING, ALERT }

@Serializable
data class NotificationDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val notificationType: NotificationType,
    val message: String,
    val isRead: Boolean? = false,
    val createdAt: String
)

@Serializable
data class CreateNotificationDto(
    val userId: Int,
    val title: String,
    val notificationType: NotificationType? = NotificationType.INFO,
    val message: String,
    val isRead: Boolean? = false,
    val createdAt: String
)

@Serializable
data class UpdateNotificationDto(
    val title: String? = null,
    val notificationType: NotificationType? = null,
    val message: String? = null,
    val isRead: Boolean? = null
)
