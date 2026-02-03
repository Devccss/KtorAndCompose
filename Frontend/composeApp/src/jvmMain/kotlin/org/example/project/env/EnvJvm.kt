package org.example.project.env

import java.io.File

actual object Env {
    private val map: MutableMap<String, String> = mutableMapOf()

    actual fun loadEnvFile(path: String) {
        try {
            val file = File(path)
            if (!file.exists()) return
            file.forEachLine { raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEachLine
                val idx = line.indexOf('=')
                if (idx <= 0) return@forEachLine
                val key = line.substring(0, idx).trim()
                var value = line.substring(idx + 1).trim()
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))
                ) {
                    value = value.substring(1, value.length - 1)
                }
                map[key] = value
            }
        } catch (_: Exception) {
            // silencioso: no romper la app si no se puede leer el .env
        }
    }

    actual fun get(key: String, default: String): String {
        return map[key] ?: System.getenv(key) ?: System.getProperty(key) ?: default
    }
}
