package repositories

import com.example.dtos.CreateNotificationDto
import com.example.dtos.NotificationDto
import com.example.dtos.UpdateNotificationDto
import io.ktor.server.plugins.BadRequestException
import models.Notifications
import models.NotificationType

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class NotificationsRepository {

    private fun resultRowToNotification(row: ResultRow): NotificationDto {
        return NotificationDto(
            id = row[Notifications.id].value,
            userId = row[Notifications.userId],
            title = row[Notifications.title],
            notificationType = row[Notifications.notificationType],
            message = row[Notifications.message],
            isRead = row[Notifications.isRead],
            createdAt = row[Notifications.createdAt].toString()
        )
    }

    fun getAll(): List<NotificationDto> = transaction {
        Notifications.selectAll().orderBy(Notifications.createdAt).map(::resultRowToNotification)
    }

    fun getById(id: Int): NotificationDto? = transaction {
        Notifications.selectAll().where { Notifications.id eq id }.singleOrNull()?.let(::resultRowToNotification)
    }

    fun create(dto: CreateNotificationDto): NotificationDto = try {
        transaction {
            val newId = Notifications.insert {
                it[userId] = dto.userId
                it[title] = dto.title
                it[notificationType] = dto.notificationType ?: NotificationType.INFO
                it[message] = dto.message
                it[isRead] = dto.isRead ?: false
            }[Notifications.id]

            NotificationDto(
                id = newId.value,
                userId = dto.userId,
                title = dto.title,
                notificationType = dto.notificationType ?: NotificationType.INFO,
                message = dto.message,
                isRead = dto.isRead ?: false,
                createdAt = LocalDateTime.now().toString()
            )
        }
    } catch (e: Exception) {
        throw BadRequestException("Error al crear la notificación: ${e.message}")
    }

    fun update(id: Int, dto: UpdateNotificationDto) {
        transaction {
            getById(id) ?: throw BadRequestException("Notificación con ID $id no existe.")
            Notifications.update({ Notifications.id eq id }) { u ->
                dto.title?.let { u[Notifications.title] = it }
                dto.notificationType?.let { u[Notifications.notificationType] = it }
                dto.message?.let { u[Notifications.message] = it }
                dto.isRead?.let { u[Notifications.isRead] = it }
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Notificación con ID $id no existe.")
        Notifications.deleteWhere { Notifications.id eq id } > 0
    }
}
