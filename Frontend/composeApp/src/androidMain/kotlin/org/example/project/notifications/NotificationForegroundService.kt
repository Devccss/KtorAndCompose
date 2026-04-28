package org.example.project.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import androidx.core.content.edit
import org.example.project.MainActivity
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession

class NotificationForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FOREGROUND_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(FOREGROUND_ID, notification)
        }
        serviceScope.launch { pollNotificationsLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    private suspend fun pollNotificationsLoop() {

        while (currentCoroutineContext().isActive) {

            val userId = UserSession.idUser
            if (userId == null || userId <= 0) {
                delay(POLL_INTERVAL_MS)
                continue
            }

            runCatching {
                RepositoryProvider.checkInitialized()
                val unreadNotifications = RepositoryProvider.notificationRepo.getUnreadNotificationsByUser(userId)

                val deliveredIds = loadDeliveredIds(userId)

                val newNotifications = unreadNotifications.filterNot { it.id in deliveredIds }



                newNotifications.forEach { notification ->
                    showSystemNotification(notification.id, notification.title, notification.message)
                }

                if (newNotifications.isNotEmpty()) {
                    deliveredIds += newNotifications.map { it.id }
                    saveDeliveredIds(userId, deliveredIds)
                }
            }

            delay(POLL_INTERVAL_MS)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val builder = notificationBuilder()
        return builder
            .setContentTitle("Notificaciones activas!")
            .setContentText(" ")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    pendingIntentFlags()
                )
            )
            .build()
    }

    private fun showSystemNotification(id: Int, title: String, message: String) {
        val notification = notificationBuilder()
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(Notification.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    id,
                    Intent(this, MainActivity::class.java),
                    pendingIntentFlags()
                )
            )
            .build()

        getSystemService(NotificationManager::class.java).notify(SYSTEM_NOTIFICATION_BASE + id, notification)
    }

    private fun notificationBuilder(): Notification.Builder {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para notificaciones de la aplicación"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun loadDeliveredIds(userId: Int): MutableSet<Int> {
        val raw = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getStringSet(deliveredKey(userId), emptySet())
            .orEmpty()

        return raw.mapNotNullTo(mutableSetOf()) { it.toIntOrNull() }
    }

    private fun saveDeliveredIds(userId: Int, ids: Set<Int>) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putStringSet(deliveredKey(userId), ids.map { it.toString() }.toSet())
        }
    }

    private fun deliveredKey(userId: Int): String = "delivered_notifications_$userId"

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    companion object {
        private const val CHANNEL_ID = "org.example.project.notifications"
        private const val CHANNEL_NAME = "Notificaciones de progreso"
        private const val PREFS_NAME = "notification_sync_prefs"
        private const val FOREGROUND_ID = 1001
        private const val SYSTEM_NOTIFICATION_BASE = 10_000
        private const val POLL_INTERVAL_MS = 5_000L

        fun start(context: Context) {
            val intent = Intent(context, NotificationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}




