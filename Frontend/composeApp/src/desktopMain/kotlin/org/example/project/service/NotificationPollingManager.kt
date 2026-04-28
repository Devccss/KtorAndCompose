package org.example.project.service

actual object NotificationPollingManager {
    actual fun start() {
        // Desktop platform - no native notifications polling needed
    }
}

