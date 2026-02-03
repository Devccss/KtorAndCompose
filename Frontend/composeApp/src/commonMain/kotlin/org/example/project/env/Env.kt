package org.example.project.env

/**
 * Expect object para manejar variables de entorno / .env de forma multiplataforma.
 * - loadEnvFile(path): intenta cargar un archivo .env (silencioso si no existe).
 * - get(key, default): obtiene el valor de la variable (mapa cargado > env vars > default).
 */
expect object Env {
    fun loadEnvFile(path: String = ".env")
    fun get(key: String, default: String = ""): String
}
