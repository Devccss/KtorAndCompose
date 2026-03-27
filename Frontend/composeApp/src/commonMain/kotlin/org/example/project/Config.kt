package org.example.project

import org.example.project.env.Env

/**
 * Devuelve la base URL para el backend leyendo la variable BASE_URL (.env o env vars).
 * Carga el archivo .env por defecto la primera vez que se invoca.
 */
private var envLoaded = false

suspend fun getBaseUrl(): String {
    if (!envLoaded) {
        Env.loadEnvFile() // intenta cargar ".env" en el working directory (silencioso si no existe)
        envLoaded = true
    }
    return Env.get("BASE_URL", "http://10.0.2.2:443")
}
