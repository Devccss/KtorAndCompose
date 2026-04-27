package repositories

import com.example.dtos.CreateNotificationDto
import com.example.dtos.FilterNotificationsDto
import com.example.dtos.NotificationDto
import com.example.dtos.UpdateNotificationDto
import io.ktor.server.plugins.BadRequestException
import models.Notifications
import models.NotificationStatus
import models.NotificationType

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class NotificationsRepository {

    private val queueDelaySeconds = 30L

    private fun parseDateTimeOrNow(value: String?): LocalDateTime {
        return value?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
    }

    private fun resultRowToNotification(row: ResultRow): NotificationDto {
        return NotificationDto(
            id = row[Notifications.id].value,
            userId = row[Notifications.userId],
            title = row[Notifications.title],
            notificationType = row[Notifications.notificationType],
            category = row[Notifications.category],
            subCategory = row[Notifications.subCategory],
            message = row[Notifications.message],
            status = row[Notifications.isRead],
            createdAt = row[Notifications.createdAt].toString()
        )
    }

    fun promoteEligibleWaitingNotifications(userId: Int? = null) = transaction {
        var waiting = Notifications.selectAll().where { Notifications.isRead eq NotificationStatus.WAITING }
        userId?.let { waiting = waiting.andWhere { Notifications.userId eq it } }

        waiting.orderBy(Notifications.createdAt).forEach { row ->
            val sameBucket = Notifications.selectAll()
                .where { Notifications.userId eq row[Notifications.userId] }
                .andWhere { Notifications.category eq row[Notifications.category] }
                .andWhere { Notifications.subCategory eq row[Notifications.subCategory] }

            val previous = sameBucket
                .andWhere { Notifications.createdAt less row[Notifications.createdAt] }
                .orderBy(Notifications.createdAt)
                .lastOrNull()

            val canShow = previous == null ||
                    !LocalDateTime.now().isBefore(previous[Notifications.createdAt].plusSeconds(queueDelaySeconds))

            if (canShow) {
                Notifications.update({ Notifications.id eq row[Notifications.id].value }) {
                    it[isRead] = NotificationStatus.UNREAD
                }
            }
        }
    }

    fun getAll(): List<NotificationDto> = transaction {
        Notifications.selectAll().orderBy(Notifications.createdAt).map(::resultRowToNotification)
    }

    fun getById(id: Int): NotificationDto? = transaction {
        Notifications.selectAll().where { Notifications.id eq id }.singleOrNull()?.let(::resultRowToNotification)
    }

    fun searchNotifications(filters: FilterNotificationsDto): List<NotificationDto> = transaction {
        var query = Notifications.selectAll()

        filters.userId?.let { query = query.andWhere { Notifications.userId eq it } }
        filters.title?.takeIf { it.isNotBlank() }?.let { query = query.andWhere { Notifications.title like "%$it%" } }
        filters.notificationType?.let { query = query.andWhere { Notifications.notificationType eq it } }
        filters.category?.let { query = query.andWhere { Notifications.category eq it } }
        filters.subCategory?.let { query = query.andWhere { Notifications.subCategory eq it } }
        filters.status?.let { query = query.andWhere { Notifications.isRead eq it } }

        query.orderBy(Notifications.createdAt).map(::resultRowToNotification)
    }

    fun create(dto: CreateNotificationDto): NotificationDto = try {
        transaction {
            val createdAt = parseDateTimeOrNow(dto.createdAt)
            val lastFromBucket = Notifications.selectAll()
                .where { Notifications.userId eq dto.userId }
                .andWhere { Notifications.category eq dto.category }
                .andWhere { Notifications.subCategory eq dto.subCategory }
                .orderBy(Notifications.createdAt)
                .lastOrNull()

            val calculatedStatus = if (
                lastFromBucket != null &&
                createdAt.isBefore(lastFromBucket[Notifications.createdAt].plusSeconds(queueDelaySeconds))
            ) {
                NotificationStatus.WAITING
            } else {
                dto.status ?: NotificationStatus.UNREAD
            }

            val newId = Notifications.insert {
                it[userId] = dto.userId
                it[title] = dto.title
                it[notificationType] = dto.notificationType ?: NotificationType.INFO
                it[category] = dto.category
                it[subCategory] = dto.subCategory
                it[message] = dto.message
                it[isRead] = calculatedStatus
                it[Notifications.createdAt] = createdAt
            }[Notifications.id]

            NotificationDto(
                id = newId.value,
                userId = dto.userId,
                title = dto.title,
                notificationType = dto.notificationType ?: NotificationType.INFO,
                category = dto.category,
                subCategory = dto.subCategory,
                message = dto.message,
                status = calculatedStatus,
                createdAt = createdAt.toString()
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
                dto.category?.let { u[Notifications.category] = it }
                dto.subCategory?.let { u[Notifications.subCategory] = it }
                dto.status?.let { u[Notifications.isRead] = it }
            }
        }
    }

    fun markAsRead(id: Int): NotificationDto = transaction {
        getById(id) ?: throw BadRequestException("Notificación con ID $id no existe.")
        Notifications.update({ Notifications.id eq id }) {
            it[isRead] = NotificationStatus.READ
        }
        getById(id) ?: throw BadRequestException("No se pudo actualizar la notificación.")
    }

    fun markAsUnread(id: Int): NotificationDto = transaction {
        getById(id) ?: throw BadRequestException("Notificación con ID $id no existe.")
        Notifications.update({ Notifications.id eq id }) {
            it[isRead] = NotificationStatus.UNREAD
        }
        getById(id) ?: throw BadRequestException("No se pudo actualizar la notificación.")
    }

    fun delete(id: Int): Boolean = transaction {
        getById(id) ?: throw BadRequestException("Notificación con ID $id no existe.")
        Notifications.deleteWhere { Notifications.id eq id } > 0
    }
}
