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

        // Envolver en runCatching para evitar que falle si el token no está establecido aún
        runCatching {
            val started = repo.startSession(
                CreateUserSessionLogDto(
                    userId = userId,
                )
            )
            UserSession.updateSessionLogId(started.id)
        }.onFailure { e ->
            println("[SessionLogTracker] ⚠️ Error al iniciar sesión de tracking: ${e.message}")
            // No propagamos el error, es no-crítico
        }
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

