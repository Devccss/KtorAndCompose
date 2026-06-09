package org.example.project.service

import org.example.project.dtos.CreateNotificationDto
import org.example.project.dtos.FilterNotificationsDto
import org.example.project.dtos.NotificationDto
import org.example.project.dtos.NotificationStatus
import org.example.project.dtos.NotificationType
import org.example.project.dtos.NotificationCategory
import org.example.project.dtos.NotificationSubCategory
import org.example.project.dtos.UpdateNotificationDto
import org.example.project.network.RepositoryProvider
import org.example.project.repository.NotificationRepo

class NotificationService(
    private val repo: NotificationRepo = RepositoryProvider.notificationRepo
) {

    suspend fun getAll(): List<NotificationDto> = repo.getAllNotifications()

    suspend fun getByUser(userId: Int, unreadOnly: Boolean = false): List<NotificationDto> {
        return if (unreadOnly) {
            repo.getUnreadNotificationsByUser(userId)
        } else {
            repo.getNotificationsByUser(userId)
        }
    }

    suspend fun getUnreadCount(userId: Int): Int = repo.getUnreadNotificationsByUser(userId).size

    suspend fun search(filters: FilterNotificationsDto): List<NotificationDto> = repo.searchNotifications(filters)

    suspend fun getById(id: Int): NotificationDto = repo.getNotificationById(id)

    suspend fun create(dto: CreateNotificationDto): NotificationDto = repo.createNotification(dto)

    suspend fun update(id: Int, dto: UpdateNotificationDto): Boolean = repo.updateNotification(id, dto)

    suspend fun markAsRead(id: Int): NotificationDto = repo.markAsRead(id)

    suspend fun markAsUnread(id: Int): NotificationDto = repo.markAsUnread(id)

    suspend fun delete(id: Int): Boolean = repo.deleteNotification(id)

    suspend fun notifyIfUserIsCloseToFinishUnits(
        userId: Int,
        remainingUnits: Int
    ): NotificationDto? {
        if (remainingUnits >= 5) return null

        return create(
            CreateNotificationDto(
                userId = userId,
                title = "Estas cerca de terminar",
                notificationType = NotificationType.INFO,
                category = NotificationCategory.PROGRESS,
                subCategory = NotificationSubCategory.UNIT_COMPLETED,
                message = "Te quedan $remainingUnits unidades para completar la app.",
                status = NotificationStatus.UNREAD
            )
        )
    }
}



