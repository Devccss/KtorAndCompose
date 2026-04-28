package org.example.project

import org.example.project.env.Env

/**
 * Devuelve la base URL para el backend leyendo la variable BASE_URL (.env o env vars).
 * Carga el archivo .env por defecto la primera vez que se invoca.
 */
private var envLoaded = false

private fun defaultBaseUrlForCurrentPlatform(): String {
    return when {
        getPlatform().name.contains("Emulator", ignoreCase = true) -> "http://10.0.2.2:8000"
        getPlatform().name.startsWith("Android") -> "http://192.168.1.6:8000"
        else -> "http://127.0.0.1:8000"
    }
}


suspend fun getBaseUrl(): String {
    if (!envLoaded) {
        Env.loadEnvFile() // intenta cargar ".env" en el working directory (silencioso si no existe)
        envLoaded = true
    }
    return Env.get("BASE_URL", defaultBaseUrlForCurrentPlatform())
}
