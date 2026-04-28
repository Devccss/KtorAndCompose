package org.example.project.service

import android.content.Context
import org.example.project.notifications.NotificationForegroundService

actual object NotificationPollingManager {
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context
    }

    actual fun start() {
        val ctx = context ?: return
        NotificationForegroundService.start(ctx)
    }
}

