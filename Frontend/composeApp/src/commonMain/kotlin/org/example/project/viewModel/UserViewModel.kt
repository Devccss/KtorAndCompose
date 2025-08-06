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
import org.example.project.dtos.LoginDto
import org.example.project.dtos.UsersDto
import org.example.project.models.Role

import org.example.project.models.Users
import org.example.project.repository.UsersRepository.UserRepo


data class UsersUiState(
    val users: List<Users>? = emptyList(),
    val currentUser: UsersDto? = null,
    val isLoading: Boolean = false,
    val registerUser: CreateUserDto? = null,
    val error: String? = null,

    )

class UserViewModel(private val repo: UserRepo) : ViewModel(), ScreenModel {
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
        loadUsers()
    }


    private fun loadUsers() {
        launchCatching(
            block = { repo.getAllUsers() },
            onSuccess = { users ->
                _state.value = UsersUiState(
                    users = users,
                    isLoading = false
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
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
                        currentUser = user,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "User not found",
                        isLoading = false
                    )
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
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
                    users = _state.value.users?.plus(added),
                    registerUser = newUser,
                    isLoading = false
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
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
                        isLoading = false
                    )
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
        )
    }

    fun updateUser(id: Int, updatedUser: Users) {
        launchCatching(
            block = { repo.updateUser(id, updatedUser) },
            onSuccess = { updated ->
                _state.value = _state.value.copy(
                    currentUser = if (_state.value.currentUser?.id == id) updated else _state.value.currentUser
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun deleteUser(id: Int) {
        launchCatching(
            block = { repo.deleteUser(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value =
                        (if (_state.value.currentUser?.id == id) null else _state.value.currentUser)?.let { it ->
                            _state.value.copy(
                                users = _state.value.users?.filterNot { it.id == id },
                                currentUser = it
                            )
                        }!!
                } else {
                    _state.value = _state.value.copy(error = "Failed to delete user")
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