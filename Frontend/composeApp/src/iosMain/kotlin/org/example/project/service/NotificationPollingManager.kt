package org.example.project.service

actual object NotificationPollingManager {
    actual fun start() {
        // iOS platform - no native notifications polling needed
    }
}

