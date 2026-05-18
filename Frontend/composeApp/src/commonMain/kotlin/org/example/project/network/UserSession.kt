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
    var idUser by mutableStateOf<Int?>(null)
        private set

    var name: String? by mutableStateOf(null)
        private set

    var token: String? by mutableStateOf(null)
        private set

    var role: Role? by mutableStateOf(null)
        private set

    var actualUnit: Int? by mutableStateOf(null)
        private set

    var sessionLogId: Int? by mutableStateOf(null)
        private set

    // Legacy: la lógica de repaso se ha movido al backend. Estas estructuras se mantienen para compatibilidad
    // pero las funciones públicas relacionadas ahora son NO-OP o están marcadas como @Deprecated.
    private val unitReviewCheckpoints = mutableMapOf<Int, Int>()
    private val reviewedExercisesByUnit = mutableMapOf<Int, MutableSet<Int>>()
    private val reviewUnitCompletionEvents = mutableSetOf<Int>()

    fun set(id: Int? = null, name: String? = null, role: Role? = null, token: String? = null, actualUnit: Int? = null) {
        if (id != null) this.idUser = id
        if (name != null) this.name = name
        if (role != null) this.role = role
        if (token != null) this.token = token
        if (actualUnit != null) this.actualUnit = actualUnit
    }

    fun updateSessionLogId(logId: Int?) {
        sessionLogId = logId
    }

    @Deprecated("La lógica de repaso ahora se ejecuta en el backend. Usar el endpoint /tests/review-status")
    fun markUnitRequiresReview(unitId: Int, completedUnitsCount: Int) {
        // NO-OP: ahora el backend calcula y persiste el estado de repaso
    }

    @Deprecated("La lógica de repaso ahora se ejecuta en el backend. Usar el endpoint /tests/review-status")
    fun clearUnitReviewRequirement(unitId: Int) {
        // NO-OP
    }

    @Deprecated("La lógica de repaso ahora se ejecuta en el backend. Usar el endpoint /tests/review-status")
    fun requiresUnitReview(unitId: Int, completedUnitsCount: Int): Boolean {
        // NO-OP -> el frontend debe consultar el backend
        return false
    }

    @Deprecated("La lógica de repaso ahora se ejecuta en el backend. Registrar ejercicios completados via API")
    fun registerReviewedExercise(unitId: Int, exerciseId: Int) {
        // NO-OP
    }

    @Deprecated("La lógica de repaso ahora se ejecuta en el backend. Consultar /tests/review-status")
    fun reviewedExercisesCount(unitId: Int): Int = reviewedExercisesByUnit[unitId]?.size ?: 0

    @Deprecated("La regla de requiredReview permanece pero la fuente de verdad es el backend")
    fun requiredReviewExercises(totalExercisesInUnit: Int): Int {
        if (totalExercisesInUnit <= 0) return 1
        return when {
            totalExercisesInUnit <= 2 -> 1
            totalExercisesInUnit <= 5 -> 2
            else -> kotlin.math.ceil(totalExercisesInUnit * 0.4f).toInt()
        }
    }

    @Deprecated("La verificación ahora se hace en el backend")
    fun isUnitReviewSatisfied(unitId: Int, totalExercisesInUnit: Int): Boolean {
        return reviewedExercisesCount(unitId) >= requiredReviewExercises(totalExercisesInUnit)
    }

    @Deprecated("Control de emisión ahora debe delegarse al backend o al repositorio")
    fun canEmitReviewUnitCompleted(unitId: Int): Boolean = unitId !in reviewUnitCompletionEvents

    @Deprecated("Control de emisión ahora debe delegarse al backend o al repositorio")
    fun markReviewUnitCompletedEmitted(unitId: Int) {
        // NO-OP
    }

    fun clear() {
        idUser = null
        name = null
        role = null
        actualUnit = null
        sessionLogId = null
        token = null
        unitReviewCheckpoints.clear()
        reviewedExercisesByUnit.clear()
        reviewUnitCompletionEvents.clear()
    }
}
