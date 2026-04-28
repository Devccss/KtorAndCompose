package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateNotificationDto
import org.example.project.dtos.FilterNotificationsDto
import org.example.project.dtos.NotificationDto
import org.example.project.dtos.NotificationStatus
import org.example.project.service.NotificationService

data class NotificationUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel(
    private val service: NotificationService
) : ViewModel(), ScreenModel {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    private var refreshJob: Job? = null

    fun loadAll() {
        launchCatching(
            block = { service.getAll() },
            onSuccess = { notifications ->
                _state.update {
                    it.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { notification -> notification.status == NotificationStatus.UNREAD },
                        error = null
                    )
                }
            }
        )
    }

    fun loadByUser(userId: Int, unreadOnly: Boolean = false) {
        launchCatching(
            block = { service.getByUser(userId, unreadOnly) },
            onSuccess = { notifications ->
                _state.update {
                    it.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { notification -> notification.status == NotificationStatus.UNREAD },
                        error = null
                    )
                }
            }
        )
    }

    fun search(filters: FilterNotificationsDto) {
        launchCatching(
            block = { service.search(filters) },
            onSuccess = { notifications ->
                _state.update {
                    it.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { notification -> notification.status == NotificationStatus.UNREAD },
                        error = null
                    )
                }
            }
        )
    }

    fun create(dto: CreateNotificationDto) {
        launchCatching(
            block = { service.create(dto) },
            onSuccess = { created ->
                _state.update { current ->
                    current.copy(
                        notifications = listOf(created) + current.notifications,
                        unreadCount = current.unreadCount + if (created.status == NotificationStatus.UNREAD) 1 else 0,
                        error = null
                    )
                }
            }
        )
    }

    fun notifyIfUserIsCloseToFinishUnits(userId: Int, remainingUnits: Int) {
        launchCatching(
            block = { service.notifyIfUserIsCloseToFinishUnits(userId, remainingUnits) },
            onSuccess = { created ->
                if (created != null) {
                    _state.update { current ->
                        current.copy(
                            notifications = listOf(created) + current.notifications,
                            unreadCount = current.unreadCount + 1,
                            error = null
                        )
                    }
                }
            }
        )
    }

    fun markAsRead(id: Int) {
        launchCatching(
            block = { service.markAsRead(id) },
            onSuccess = { updated ->
                replaceNotification(updated)
            }
        )
    }

    fun markAsUnread(id: Int) {
        launchCatching(
            block = { service.markAsUnread(id) },
            onSuccess = { updated ->
                replaceNotification(updated)
            }
        )
    }

    fun delete(id: Int) {
        launchCatching(
            block = { service.delete(id) },
            onSuccess = { deleted ->
                if (deleted) {
                    _state.update { current ->
                        val removed = current.notifications.firstOrNull { it.id == id }
                        current.copy(
                            notifications = current.notifications.filterNot { it.id == id },
                            unreadCount = current.unreadCount - if (removed?.status == NotificationStatus.UNREAD) 1 else 0,
                            error = null
                        )
                    }
                }
            }
        )
    }

    fun startAutoRefresh(userId: Int, unreadOnly: Boolean = true) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                runCatching { loadByUser(userId, unreadOnly) }
                delay(60_000)
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun replaceNotification(updated: NotificationDto) {
        _state.update { current ->
            val notifications = current.notifications.map { if (it.id == updated.id) updated else it }
            current.copy(
                notifications = notifications,
                unreadCount = notifications.count { it.status == NotificationStatus.UNREAD },
                error = null
            )
        }
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit = { error ->
            _state.update { it.copy(error = error.message) }
        }
    ) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            onSuccess(block())
        } catch (e: Exception) {
            onError(e)
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }
}



