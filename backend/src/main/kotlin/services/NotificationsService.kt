package com.example.services

import com.example.dtos.CreateNotificationDto
import com.example.dtos.FilterNotificationsDto
import com.example.dtos.NotificationDto
import com.example.dtos.UpdateNotificationDto
import models.NotificationCategory
import models.NotificationStatus
import models.NotificationSubCategory
import models.NotificationType
import repositories.NotificationsRepository

class NotificationsService(private val repo: NotificationsRepository) {
    fun getAll(): List<NotificationDto> {
        repo.promoteEligibleWaitingNotifications()
        return repo.getAll()
    }

    fun getById(id: Int): NotificationDto? {
        repo.promoteEligibleWaitingNotifications()
        return repo.getById(id)
    }

    fun searchNotifications(filters: FilterNotificationsDto): List<NotificationDto> {
        repo.promoteEligibleWaitingNotifications(filters.userId)
        return repo.searchNotifications(filters)
    }

    fun create(dto: CreateNotificationDto): NotificationDto {
        repo.promoteEligibleWaitingNotifications(dto.userId)
        return repo.create(dto)
    }

    fun update(id: Int, dto: UpdateNotificationDto) = repo.update(id, dto)
    fun markAsRead(id: Int): NotificationDto {
        val updated = repo.markAsRead(id)
        repo.promoteEligibleWaitingNotifications(updated.userId)
        return updated
    }

    fun markAsUnread(id: Int): NotificationDto = repo.markAsUnread(id)

    fun notifyIfUserIsCloseToFinishUnits(userId: Int, remainingUnits: Int): NotificationDto? {
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

    fun delete(id: Int): Boolean = repo.delete(id)
}
