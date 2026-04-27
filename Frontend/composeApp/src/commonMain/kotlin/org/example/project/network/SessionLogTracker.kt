package org.example.project.network

import org.example.project.dtos.CloseUserSessionLogDto
import org.example.project.dtos.CreateUserSessionLogDto
import org.example.project.dtos.SessionEndReason

object SessionLogTracker {

    suspend fun onUserLoggedIn(userId: Int) {
        val repo = RepositoryProvider.sessionLogRepo

        // Cierra cualquier sesion abierta previa del usuario antes de iniciar una nueva.
        runCatching {
            repo.closeOpenSessionByUserId(
                userId,
                CloseUserSessionLogDto(
                    endReason = SessionEndReason.UNKNOWN
                )
            )
        }

        val started = repo.startSession(
            CreateUserSessionLogDto(
                userId = userId,
            )
        )
        UserSession.updateSessionLogId(started.id)
    }

    suspend fun closeForLogout(userId: Int?) {
        val safeUserId = userId ?: return
        val repo = RepositoryProvider.sessionLogRepo

        runCatching {
            repo.closeOpenSessionByUserId(
                safeUserId,
                CloseUserSessionLogDto(
                    endReason = SessionEndReason.LOGOUT
                )
            )
        }

        UserSession.updateSessionLogId(null)
    }

    suspend fun closeForAppClosed(userId: Int?) {
        val safeUserId = userId ?: return
        val repo = RepositoryProvider.sessionLogRepo

        runCatching {
            repo.closeOpenSessionByUserId(
                safeUserId,
                CloseUserSessionLogDto(

                    endReason = SessionEndReason.APP_CLOSED
                )
            )
        }

        UserSession.updateSessionLogId(null)
    }
}

