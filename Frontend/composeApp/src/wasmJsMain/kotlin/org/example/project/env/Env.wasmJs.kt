package org.example.project.env

actual object Env {
    actual fun loadEnvFile(path: String) {
    }

    actual fun get(key: String, default: String): String {
        return default
    }
}