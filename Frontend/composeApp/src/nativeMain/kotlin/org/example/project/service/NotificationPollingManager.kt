package org.example.project.service

actual object NotificationPollingManager {
    actual fun start() {
        // Native platform - no notifications polling needed
    }
}

