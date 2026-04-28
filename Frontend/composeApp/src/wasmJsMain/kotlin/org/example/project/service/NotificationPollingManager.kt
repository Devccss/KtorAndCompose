package org.example.project.service

actual object NotificationPollingManager {
    actual fun start() {
        // WASM JS platform - no native notifications polling needed
    }
}

