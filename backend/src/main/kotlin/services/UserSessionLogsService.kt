package com.example.services

import com.example.dtos.CloseUserSessionLogDto
import com.example.dtos.CreateUserSessionLogDto
import com.example.dtos.FilterUserSessionLogsDto
import com.example.dtos.UserSessionLogDto
import com.example.dtos.WeeklySessionMetricDto
import repositories.UserSessionLogsRepository

class UserSessionLogsService(private val repository: UserSessionLogsRepository) {

    fun startSession(dto: CreateUserSessionLogDto): UserSessionLogDto = repository.create(dto)

    fun closeSessionById(sessionId: Int, dto: CloseUserSessionLogDto): UserSessionLogDto =
        repository.closeBySessionId(sessionId, dto)

    fun closeOpenSessionByUserId(userId: Int, dto: CloseUserSessionLogDto): UserSessionLogDto =
        repository.closeOpenSessionByUserId(userId, dto)

    fun getSessionById(id: Int): UserSessionLogDto? = repository.getById(id)

    fun getSessionsByUserId(userId: Int): List<UserSessionLogDto> = repository.getByUserId(userId)

    fun getOpenSessionByUserId(userId: Int): UserSessionLogDto? = repository.getOpenSessionByUserId(userId)

    fun filter(dto: FilterUserSessionLogsDto): List<UserSessionLogDto> = repository.filter(dto)

    fun getWeeklyMetrics(studentId: Boolean? = true, fromDate: String?, toDate: String?): List<WeeklySessionMetricDto> =
        repository.weeklyMetrics(studentId,fromDate, toDate)
}

