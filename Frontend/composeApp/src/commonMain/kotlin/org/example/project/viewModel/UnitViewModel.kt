package org.example.project.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateUnitCompletedDto
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUnitDto
import org.example.project.dtos.UserDto
import org.example.project.repository.UnitRepo


data class UnitUiState(
    val units: List<UnitDto> = emptyList(),
    val actualUnit: UnitDto? = null,
    val currentUser: UserDto? = null,
    
    val unitsCompleted: List<UnitDto> = emptyList(),
    val isLoading: Boolean = false,
    val registerUser: CreateUserDto? = null,
    var error: String? = null,

    )

class UnitViewModel(
    private val unitRepo: UnitRepo,
    val unitId: Int? = null,
    private val autoLoad: Boolean = true
) : ViewModel(),
    ScreenModel {
    private val _state = MutableStateFlow(
        UnitUiState(
            isLoading = true,
        )
    )
    val state: StateFlow<UnitUiState> = _state

    var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        _state.value = _state.value.copy(error = message)
    }

    fun isUnitCompleted(unitId: Int): Boolean = _state.value.unitsCompleted.any { it.id == unitId }

    fun completedUnitIds(): Set<Int> = _state.value.unitsCompleted.mapNotNull { it.id }.toSet()

    fun getAllUnits() {
        launchCatching(
            block = { unitRepo.getAllUnits() },
            onSuccess = { units ->
                _state.value = _state.value.copy(units = units)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, units = emptyList())
            }
        )
    }

    init {
        launchCatching(
            block = {
                true
            },
            onSuccess = {
                _state.value = _state.value.copy(isLoading = false)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun actualNull() {
        _state.value = _state.value.copy(actualUnit = null)
    }

    fun getUnitById(id: Int) {
        launchCatching(
            block = { unitRepo.getUnitById(id) },
            onSuccess = { unit ->
                if (unit != null) {
                    _state.value = _state.value.copy(
                        actualUnit = unit,
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Unit not found",

                        )
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                    )
            }
        )
    }

    fun getUnitByTestId(testId: Int) {
        launchCatching(
            block = { unitRepo.getUnitByTestId(testId) },
            onSuccess = { unit ->

                _state.value = _state.value.copy(
                    actualUnit = unit,
                )

            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message
                )
            }
        )
    }

    fun searchUnits(filters: FilterUnitsDto) {
        val normalized = filters.copy(name = filters.name?.trim()?.takeIf { it.isNotEmpty() })
        val hasFilters = normalized.name != null || normalized.difficulty != null || normalized.isActive != null
        launchCatching(
            block = {
                if (hasFilters) unitRepo.searchUnits(normalized)
                else unitRepo.getAllUnits()
            },
            onSuccess = { units ->
                _state.value = _state.value.copy(units = units)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, units = emptyList())
            }
        )
    }

    fun createUnit(newUnit: CreateUnitDto) {
        launchCatching(
            block = {
                if (newUnit.name.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "El nombre no puede estar vacío"
                    )
                    throw IllegalArgumentException("El nombre no puede estar vacío")
                }
                if (newUnit.description.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "La descripción no puede estar vacía"
                    )
                    throw IllegalArgumentException("La descripción no puede estar vacía")
                }
                unitRepo.createUnit(newUnit)
            },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    units = _state.value.units.plus(added),

                    )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                    )
            }
        )
    }

    // Nueva función para reordenar múltiples unidades
    fun updateUnitsOrder(orders: List<Pair<Int, Int>>) {
        launchCatching(
            block = {
                unitRepo.reorderUnits(orders)
            },
            onSuccess = {
                // Refrescar la lista para asegurar consistencia
                getAllUnits()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al reordenar: ${error.message}")
            }
        )
    }

    fun updateUnit(id: Int, updatedUnit: UpdateUnitDto) {
        launchCatching(
            block = { unitRepo.updateUnit(id, updatedUnit) },
            onSuccess = {
                getUnitById(id)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deleteUnit(id: Int) {
        launchCatching(
            block = { unitRepo.deleteUnit(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        units = _state.value.units.filterNot { it.id == id }
                    )
                } else {
                    _state.value = _state.value.copy(error = "Error al eliminar la unididad")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    //CompleteUnits
    
    fun getAllUnitsCompletedByUserId(userId: Int) {
        launchCatching(
            block = { unitRepo.getAllCompletedUnitsByUserId(userId) },
            onSuccess = { units ->
                _state.value = _state.value.copy(unitsCompleted = units)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, unitsCompleted = emptyList())
            }
        )
    }
    fun createUnitCompleted(dto: CreateUnitCompletedDto) {
        launchCatching(
            block = { unitRepo.createUnitCompleted(dto) },
            onSuccess = { unit ->
                generalMessage = "Unidad marcada como completada"
                _state.value = _state.value.copy(
                    unitsCompleted = _state.value.unitsCompleted.plus(unit)
                )
            },
            onError = { error ->
                generalMessage = "Error: ${error.message}"
            }
        )
    }

    fun ensureUnitCompletedIfFullySolved(
        userId: Int,
        unitId: Int,
        totalExercises: Int,
        solvedExercises: Int,
        completedAt: String,
    ) {
        if (totalExercises !in 1..solvedExercises) return

        val alreadyCompleted = _state.value.unitsCompleted.any { it.id == unitId }
        if (alreadyCompleted) return

        createUnitCompleted(
            CreateUnitCompletedDto(
                userId = userId,
                unitId = unitId
            )
        )
    }
    fun deleteUnitsCompleted(completedId: Int) {
        launchCatching(
            block = { unitRepo.deleteUnitsCompleted(completedId) },
            onSuccess = { success ->
                if (success) {
                    generalMessage = "Unidad desmarcada como completada"
                } else {
                    generalMessage = "Error al desmarcar la unidad como completada"
                }
            },
            onError = { error ->
                generalMessage = "Error: ${error.message}"
            }
        )
    }
    


    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            onSuccess(block())
            _state.value = _state.value.copy(isLoading = false)

        } catch (e: Exception) {
            onError(e)
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }
}