package org.example.project.dtos

import kotlinx.serialization.Serializable

enum class SessionEndReason { LOGOUT, APP_CLOSED, TIMEOUT, UNKNOWN }

@Serializable
data class UserSessionLogDto(
    val id: Int,
    val userId: Int,
    val loginAt: String,
    val logoutAt: String? = null,
    val endReason: SessionEndReason? = null,
    val durationSeconds: Long? = null,
    val createdAt: String
)

@Serializable
data class CreateUserSessionLogDto(
    val userId: Int,
    val loginAt: String? = null
)

@Serializable
data class CloseUserSessionLogDto(
    val logoutAt: String? = null,
    val endReason: SessionEndReason? = SessionEndReason.LOGOUT
)

@Serializable
data class FilterUserSessionLogsDto(
    val userId: Int? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val onlyOpen: Boolean? = null
)

@Serializable
data class WeeklySessionMetricDto(
    val weekStart: String,
    val activeUsers: Int,
    val totalSessions: Int,
    val totalDurationSeconds: Long,
    val averageDurationSeconds: Long
)

