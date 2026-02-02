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
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUnitDto
import org.example.project.dtos.UserDto
import org.example.project.repository.UnitRepo


data class UnitUiState(
    val users: List<UserDto> = emptyList(),
    val unit: List<UnitDto> = emptyList(),
    val currentUser: UserDto? = null,
    val isLoading: Boolean = false,
    val registerUser: CreateUserDto? = null,
    var error: String? = null,

    )

class UnitViewModel(private val unitRepo: UnitRepo) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(
        UsersUiState(
            isLoading = true,
        )
    )
    val state: StateFlow<UsersUiState> = _state

    var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        generalMessage = message
    }

    init {
        launchCatching(
            block = { unitRepo.getAllUnits()},
            onSuccess = { unit ->

                _state.value = _state.value.copy(unit = unit)
                loadUsers()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message, unit = emptyList())
            }
        )
    }


     private fun getAllLevels() {
        launchCatching(
            block = { unitRepo.getAllUnits() },
            onSuccess = { unit ->
                if(unit.isNotEmpty()){
                    _state.value = _state.value.copy(
                        unit = unit,
                    )
                }else{

                    _state.value = _state.value.copy(
                        error = "No se encontraron unidades",
                        unit = emptyList()
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

     private fun loadUsers() {
        launchCatching(
            block = { unitRepo.getAllUnits() },
            onSuccess = { unit ->
                _state.value = _state.value.copy(
                    unit = unit,
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                )
            }
        )
    }

    private fun getUnitById(id: Int) {
        launchCatching(
            block = { unitRepo.getUnitById(id) },
            onSuccess = { unit ->
                if (unit != null) {
                    _state.value = _state.value.copy(
                        unit = _state.value.unit.map {
                            if (it.id == unit.id) UnitDto(
                                id = unit.id,
                                name = unit.name,
                                difficulty = unit.difficulty,
                                description = unit.description,
                                orderUnit = unit.orderUnit,
                                isActive = unit.isActive,
                                createdAt = unit.createdAt
                            ) else it
                        },
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "User not found",

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

    fun createUnit(newUnit: CreateUnitDto) {
        launchCatching(
            block = {
                if (newUnit.name.isEmpty()) {
                    throw IllegalArgumentException("El nombre no puede estar vacío")
                }
                if (newUnit.description.isEmpty()) {
                    throw IllegalArgumentException("La descripción no puede estar vacía")
                }
                if (newUnit.orderUnit == null || newUnit.orderUnit <= 0) {
                    throw IllegalArgumentException("El orden de la unidad debe ser un número positivo")
                }

                unitRepo.createUnit(newUnit)
            },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    unit = _state.value.unit.plus(added),

                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                )
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
                        unit = _state.value.unit.filterNot { it.id == id }
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

    fun logout() {
        _state.value = _state.value.copy(currentUser = null)
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