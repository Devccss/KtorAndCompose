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
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUsersDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UserDto
import org.example.project.repository.UnitRepo
import org.example.project.repository.UserRepo


data class UsersUiState(
    val users: List<UserDto> = emptyList(),
    val unit: List<UnitDto> = emptyList(),
    val currentUser: UserDto? = null,
    val isLoading: Boolean = false,
    val registerUser: CreateUserDto? = null,
    var error: String? = null,

    )

class UserViewModel(private val repo: UserRepo, private val unitRepo: UnitRepo) : ViewModel(), ScreenModel {
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
            block = { repo.getAllUsers() },
            onSuccess = { users ->
                _state.value = _state.value.copy(
                    users = users,
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                )
            }
        )
    }

    fun getFilterUsers(filters: FilterUsersDto) {
        launchCatching(
            block = { repo.getFilterUsers(filters) },
            onSuccess = { users ->
                if (users != null) {
                    if(users.isNotEmpty()){
                        _state.value = _state.value.copy(
                            users = users,
                        )
                    }else{

                        _state.value = _state.value.copy(
                            error = "No se encontraron usuarios con esos filtros",
                            users = emptyList()
                        )
                    }
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                )
            }
        )
    }

    fun getUserById(id: Int) {
        launchCatching(
            block = { repo.getUserById(id) },
            onSuccess = { user ->
                if (user != null) {
                    _state.value = _state.value.copy(
                        users = _state.value.users + UserDto(
                            id = user.id,
                            name = user.name,
                            email = user.email,
                            password = user.password,
                            currentUnitId = user.currentUnitId,
                            createdAt = user.createdAt
                        )
                    )
                    _state.value = _state.value.copy(
                        currentUser = user,
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

     fun getUserByEmail(email: String) {
        launchCatching(
            block = { repo.getUserByEmail(email) },
            onSuccess = { user ->
                if (user != null) {
                    _state.value = _state.value.copy(
                        users = _state.value.users.map {
                            if (it.id == user.id) UserDto(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                password = user.password,
                                currentUnitId = user.currentUnitId,
                                createdAt = user.createdAt
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

    fun registerUser(newUser: CreateUserDto) {
        launchCatching(
            block = {
                if (newUser.name.isEmpty()) {
                    throw IllegalArgumentException("El nombre no puede estar vacío")
                }
                if (newUser.email.isEmpty() || !newUser.email.contains("@")) {
                    throw IllegalArgumentException("El email es invalido")
                }
                if (newUser.password.isEmpty()) {
                    throw IllegalArgumentException("La contraseña no puede estar vacía")
                }
                repo.createUser(newUser)
            },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    users = _state.value.users.plus(added),
                    registerUser = newUser,

                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,

                )
            }
        )
    }

    fun login(loginDto: LoginDto) {
        launchCatching(
            block = {
                repo.loginUser(loginDto)
            },
            onSuccess = { user ->
                println("------Z USER: $user")
                user.let {

                    _state.value = _state.value.copy(
                        currentUser = user,
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

    fun updateUser(id: Int, updatedUser: UserDto) {
        launchCatching(
            block = { repo.updateUser(id, updatedUser) },
            onSuccess = { updated ->
                getUserByEmail(updated.email)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updateUserCurrentUnit(userId: Int, unitId: Int) {
        launchCatching(
            block = {
                val user = repo.getUserById(userId)
                    ?: throw IllegalStateException("No se encontro el usuario con ID $userId")
                repo.updateUser(
                    userId,
                    user.copy(currentUnitId = unitId)
                )
            },
            onSuccess = { updated ->
                _state.value = _state.value.copy(currentUser = updated)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al actualizar unidad actual: ${error.message}")
            }
        )
    }

    fun deleteUser(id: Int) {
        launchCatching(
            block = { repo.deleteUser(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(
                        users = _state.value.users.filterNot { it.id == id },
                        currentUser = if (_state.value.currentUser?.id == id) null else _state.value.currentUser
                    )
                } else {
                    _state.value = _state.value.copy(error = "Error al eliminar el usuario")
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