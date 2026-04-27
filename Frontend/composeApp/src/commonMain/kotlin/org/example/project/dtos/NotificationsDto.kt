package org.example.project.dtos
import kotlinx.serialization.Serializable
enum class NotificationType { INFO, WARNING, ALERT }

enum class NotificationStatus { UNREAD, READ, WAITING }
enum class NotificationCategory { PROGRESS, REMINDER, SYSTEM }
enum class NotificationSubCategory {
    UNIT_COMPLETED, EXERCISE_FAILED, TEST_REMINDER, SYSTEM_MAINTENANCE, OTHER
}

@Serializable
data class NotificationDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val notificationType: NotificationType,
    val category: NotificationCategory,
    val subCategory: NotificationSubCategory,
    val message: String,
    val status: NotificationStatus,
    val createdAt: String
)

@Serializable
data class CreateNotificationDto(
    val userId: Int,
    val title: String,
    val notificationType: NotificationType? = NotificationType.INFO,
    val category: NotificationCategory = NotificationCategory.PROGRESS,
    val subCategory: NotificationSubCategory = NotificationSubCategory.OTHER,
    val message: String,
    val status: NotificationStatus? = null,
    val createdAt: String? = null
)

@Serializable
data class UpdateNotificationDto(
    val title: String? = null,
    val notificationType: NotificationType? = null,
    val message: String? = null,
    val category: NotificationCategory? = null,
    val subCategory: NotificationSubCategory? = null,
    val status: NotificationStatus? = null
)

@Serializable
data class FilterNotificationsDto(
    val userId: Int? = null,
    val title: String? = null,
    val notificationType: NotificationType? = null,
    val category: NotificationCategory? = null,
    val subCategory: NotificationSubCategory? = null,
    val status: NotificationStatus? = null
)

