package org.example.project.env

actual object Env {
    private val map: MutableMap<String, String> = mutableMapOf()

    actual fun loadEnvFile(path: String) {
        // En Android no intentamos leer un .env en tiempo de ejecución por defecto.
        // Si necesitas soporte, carga el archivo desde assets/context y parsea igual que en JVM.
        // Mantenemos el método para compatibilidad y futuras extensiones.
    }

    actual fun get(key: String, default: String): String {
        return map[key] ?: System.getenv(key) ?: default
    }
}
