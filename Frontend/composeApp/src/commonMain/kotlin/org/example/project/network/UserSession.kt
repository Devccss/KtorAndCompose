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

    var role: Role? by mutableStateOf(null)
        private set

    var actualUnit: Int? by mutableStateOf(null)
        private set

    var sessionLogId: Int? by mutableStateOf(null)
        private set

    private val unitReviewCheckpoints = mutableMapOf<Int, Int>()
    private val reviewedExercisesByUnit = mutableMapOf<Int, MutableSet<Int>>()
    private val reviewUnitCompletionEvents = mutableSetOf<Int>()

    fun set(id: Int,name: String?, role: Role?, actualUnit: Int?) {

        this.idUser = id
        this.name = name
        this.role = role
        this.actualUnit = actualUnit
    }

    fun updateSessionLogId(logId: Int?) {
        sessionLogId = logId
    }

    fun markUnitRequiresReview(unitId: Int, completedUnitsCount: Int) {
        unitReviewCheckpoints[unitId] = completedUnitsCount
        reviewedExercisesByUnit.remove(unitId)
        reviewUnitCompletionEvents.remove(unitId)
    }

    fun clearUnitReviewRequirement(unitId: Int) {
        unitReviewCheckpoints.remove(unitId)
        reviewedExercisesByUnit.remove(unitId)
        reviewUnitCompletionEvents.remove(unitId)
    }

    fun requiresUnitReview(unitId: Int, completedUnitsCount: Int): Boolean {
        val checkpoint = unitReviewCheckpoints[unitId] ?: return false
        return completedUnitsCount <= checkpoint
    }

    fun registerReviewedExercise(unitId: Int, exerciseId: Int) {
        val reviewed = reviewedExercisesByUnit.getOrPut(unitId) { mutableSetOf() }
        reviewed += exerciseId
    }

    fun reviewedExercisesCount(unitId: Int): Int = reviewedExercisesByUnit[unitId]?.size ?: 0

    fun requiredReviewExercises(totalExercisesInUnit: Int): Int {
        if (totalExercisesInUnit <= 0) return 1
        return when {
            totalExercisesInUnit <= 2 -> 1
            totalExercisesInUnit <= 5 -> 2
            else -> kotlin.math.ceil(totalExercisesInUnit * 0.4f).toInt()
        }
    }

    fun isUnitReviewSatisfied(unitId: Int, totalExercisesInUnit: Int): Boolean {
        val required = requiredReviewExercises(totalExercisesInUnit)
        return reviewedExercisesCount(unitId) >= required
    }

    fun canEmitReviewUnitCompleted(unitId: Int): Boolean = unitId !in reviewUnitCompletionEvents

    fun markReviewUnitCompletedEmitted(unitId: Int) {
        reviewUnitCompletionEvents += unitId
    }

    fun clear() {
        idUser = null
        name = null
        role = null
        actualUnit = null
        sessionLogId = null
        unitReviewCheckpoints.clear()
        reviewedExercisesByUnit.clear()
        reviewUnitCompletionEvents.clear()
    }
}
