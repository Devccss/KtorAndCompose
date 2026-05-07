package repositories

import com.example.dtos.CloseUserSessionLogDto
import com.example.dtos.CreateUserSessionLogDto
import com.example.dtos.FilterUserSessionLogsDto
import com.example.dtos.UserSessionLogDto
import com.example.dtos.WeeklySessionMetricDto
import io.ktor.server.plugins.BadRequestException
import models.Role
import models.UserSessionLogs
import models.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class UserSessionLogsRepository {

    private fun parseDateTimeOrNow(value: String?): LocalDateTime {
        return value?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
    }

    private fun mapToDto(row: ResultRow): UserSessionLogDto {
        val loginAt = row[UserSessionLogs.loginAt]
        val logoutAt = row[UserSessionLogs.logoutAt]

        return UserSessionLogDto(
            id = row[UserSessionLogs.id].value,
            userId = row[UserSessionLogs.userId],
            loginAt = loginAt.toString(),
            logoutAt = logoutAt?.toString(),
            endReason = row[UserSessionLogs.endReason],
            durationSeconds = if (logoutAt != null) Duration.between(loginAt, logoutAt).seconds else null,
            createdAt = row[UserSessionLogs.createdAt].toString()
        )
    }

    fun getById(id: Int): UserSessionLogDto? = transaction {
        UserSessionLogs.selectAll().where { UserSessionLogs.id eq id }.singleOrNull()?.let(::mapToDto)
    }

    fun getByUserId(userId: Int): List<UserSessionLogDto> = transaction {
        UserSessionLogs.selectAll()
            .where { UserSessionLogs.userId eq userId }
            .orderBy(UserSessionLogs.loginAt)
            .map(::mapToDto)
    }

    fun getOpenSessionByUserId(userId: Int): UserSessionLogDto? = transaction {
        UserSessionLogs.selectAll()
            .where { UserSessionLogs.userId eq userId }
            .andWhere { UserSessionLogs.logoutAt.isNull() }
            .orderBy(UserSessionLogs.loginAt)
            .lastOrNull()
            ?.let(::mapToDto)
    }

    fun create(dto: CreateUserSessionLogDto): UserSessionLogDto = transaction {
        val userExists = Users.selectAll().where { Users.id eq dto.userId }.count() > 0
        if (!userExists) throw BadRequestException("Usuario con ID ${dto.userId} no existe.")

        val hasOpenSession = UserSessionLogs.selectAll()
            .where { UserSessionLogs.userId eq dto.userId }
            .andWhere { UserSessionLogs.logoutAt.isNull() }
            .count() > 0

        if (hasOpenSession) {
            throw BadRequestException("Ya existe una sesion abierta para este usuario.")
        }

        val loginAt = parseDateTimeOrNow(dto.loginAt)

        val newId = UserSessionLogs.insert {
            it[userId] = dto.userId
            it[UserSessionLogs.loginAt] = loginAt
            it[createdAt] = LocalDateTime.now()
        }[UserSessionLogs.id]

        getById(newId.value) ?: throw BadRequestException("No se pudo crear el log de sesion.")
    }

    fun closeBySessionId(sessionId: Int, dto: CloseUserSessionLogDto): UserSessionLogDto = transaction {
        val existing = UserSessionLogs.selectAll()
            .where { UserSessionLogs.id eq sessionId }
            .singleOrNull() ?: throw BadRequestException("Sesion con ID $sessionId no existe.")

        if (existing[UserSessionLogs.logoutAt] != null) {
            throw BadRequestException("La sesion ya esta cerrada.")
        }

        val logoutAt = parseDateTimeOrNow(dto.logoutAt)
        val loginAt = existing[UserSessionLogs.loginAt]
        if (logoutAt.isBefore(loginAt)) {
            throw BadRequestException("logoutAt no puede ser anterior a loginAt.")
        }

        UserSessionLogs.update({ UserSessionLogs.id eq sessionId }) {
            it[UserSessionLogs.logoutAt] = logoutAt
            it[endReason] = dto.endReason
        }

        getById(sessionId) ?: throw BadRequestException("No se pudo cerrar la sesion.")
    }

    fun closeOpenSessionByUserId(userId: Int, dto: CloseUserSessionLogDto): UserSessionLogDto = transaction {
        val openSession = UserSessionLogs.selectAll()
            .where { UserSessionLogs.userId eq userId }
            .andWhere { UserSessionLogs.logoutAt.isNull() }
            .orderBy(UserSessionLogs.loginAt)
            .lastOrNull() ?: throw BadRequestException("No hay sesion abierta para el usuario $userId.")

        closeBySessionId(openSession[UserSessionLogs.id].value, dto)
    }

    fun filter(dto: FilterUserSessionLogsDto): List<UserSessionLogDto> = transaction {
        var query = UserSessionLogs.selectAll()

        dto.userId?.let { userId ->
            query = query.where { UserSessionLogs.userId eq userId }
        }

        dto.fromDate?.let { fromDate ->
            val from = LocalDateTime.parse(fromDate)
            query = query.andWhere { UserSessionLogs.loginAt greaterEq from }
        }

        dto.toDate?.let { toDate ->
            val to = LocalDateTime.parse(toDate)
            query = query.andWhere { UserSessionLogs.loginAt lessEq to }
        }

        dto.onlyOpen?.let { onlyOpen ->
            query = if (onlyOpen) {
                query.andWhere { UserSessionLogs.logoutAt.isNull() }
            } else {
                query.andWhere { UserSessionLogs.logoutAt.isNotNull() }
            }
        }

        query.orderBy(UserSessionLogs.loginAt).map(::mapToDto)
    }

    fun weeklyMetrics(studentId: Boolean? = true, fromDate: String?, toDate: String?): List<WeeklySessionMetricDto> = transaction {
        val from = fromDate?.let { LocalDate.parse(it).atStartOfDay() }
        val to = toDate?.let { LocalDate.parse(it).atTime(23, 59, 59) }

        var query = UserSessionLogs.selectAll().where { UserSessionLogs.logoutAt.isNotNull() }

        studentId?.let {
            val userIds = Users.selectAll().where { Users.role eq Role.STUDENT }.map { it[Users.id].value }
            query = query.andWhere { UserSessionLogs.userId inList userIds }
        }
        from?.let { query = query.andWhere { UserSessionLogs.loginAt greaterEq it } }
        to?.let { query = query.andWhere { UserSessionLogs.loginAt lessEq it } }

        val grouped = query.map(::mapToDto).groupBy { session ->
            val loginDate = LocalDateTime.parse(session.loginAt).toLocalDate()
            loginDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        grouped.entries
            .sortedBy { it.key }
            .map { (weekStart, sessions) ->
                val durations = sessions.mapNotNull { it.durationSeconds }
                val totalDuration = durations.sum()
                WeeklySessionMetricDto(
                    weekStart = weekStart.toString(),
                    activeUsers = sessions.map { it.userId }.toSet().size,
                    totalSessions = sessions.size,
                    totalDurationSeconds = totalDuration,
                    averageDurationSeconds = if (durations.isNotEmpty()) totalDuration / durations.size else 0L
                )
            }
    }
}

