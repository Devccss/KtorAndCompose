package org.example.project.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.project.dtos.Role

/**
 * Sesión simple en memoria para mantener nombre/rol del usuario logueado.
 * Se usa desde ReusableBottomBar para recuperar datos si la pantalla no los pasa.
 */
object UserSession {
    var name: String? by mutableStateOf(null)
        private set

    var role: Role? by mutableStateOf(null)
        private set

    fun set(name: String?, role: Role?) {
        this.name = name
        this.role = role
    }

    fun clear() {
        name = null
        role = null
    }
}
