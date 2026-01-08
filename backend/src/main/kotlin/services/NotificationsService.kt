package com.example.services

import com.example.dtos.CreateNotificationDto
import com.example.dtos.NotificationDto
import com.example.dtos.UpdateNotificationDto
import repositories.NotificationsRepository

class NotificationsService(private val repo: NotificationsRepository) {
    fun getAll(): List<NotificationDto> = repo.getAll()
    fun getById(id: Int): NotificationDto? = repo.getById(id)
    fun create(dto: CreateNotificationDto): NotificationDto = repo.create(dto)
    fun update(id: Int, dto: UpdateNotificationDto) = repo.update(id, dto)
    fun delete(id: Int): Boolean = repo.delete(id)
}
