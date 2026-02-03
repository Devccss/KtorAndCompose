package org.example.project.network

import io.ktor.client.HttpClient
import org.example.project.repository.UnitRepo
import org.example.project.repository.UserRepo


object RepositoryProvider {
    private var initialized = false

    // Repositorios respaldados por nullable para evitar lateinit exceptions
    private var _userRepo: UserRepo? = null
    val userRepo: UserRepo
        get() = _userRepo ?: throw IllegalStateException(
            "RepositoryProvider.userRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )

    private var _unitRepo: UnitRepo? = null
    val unitRepo: UnitRepo
        get() = _unitRepo ?: throw IllegalStateException(
            "RepositoryProvider.unitRepo not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories."
        )


    /**
     * Inicializa todos los repositorios con el HttpClient y baseUrl.
     * Llamar una vez al arrancar la app (antes de resolver dependencias).
     */
    fun init(httpClient: HttpClient, baseUrl: String) {
        if (initialized) return

        _userRepo = UserRepo(httpClient, baseUrl)
        _unitRepo = UnitRepo(httpClient, baseUrl)

        initialized = true
    }

    /**
     * Limpia las referencias para permitir reinicialización (útil en tests).
     */
    fun clear() {
        // No se llaman destructores específicos; solo liberamos referencias
        initialized = false
        _userRepo = null
        _unitRepo = null
    }

    fun checkInitialized() {
        check(initialized) { "RepositoryProvider not initialized. Call RepositoryProvider.init(httpClient, baseUrl) before using repositories." }
    }
}