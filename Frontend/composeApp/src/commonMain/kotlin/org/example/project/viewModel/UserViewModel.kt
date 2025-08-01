package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import org.example.project.models.Users
import org.example.project.repository.UsersRepository.UserRepo


data class UsersUiState(
    val users: List<Users> = emptyList(),
    val currentUser: Users? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserViewModel(private val repo: UserRepo) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(UsersUiState(isLoading = true))
    val state: StateFlow<UsersUiState> = _state

    init {
        loadUsers()
    }

    private fun loadUsers() {
        launchCatching(
            block = { repo.getAllUsers() },
            onSuccess = { users ->
                _state.value = _state.value.copy(
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

    fun addUser(newUser: Users) {
        launchCatching(
            block = { repo.createUser(newUser) },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    users = _state.value.users + added
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
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

    fun registerUser(newUser: Users) {
        launchCatching(
            block = { repo.createUser(newUser) },
            onSuccess = { added ->
                _state.value = _state.value.copy(
                    users = _state.value.users + added,
                    currentUser = added,
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

    fun login(email: String, password: String) {
        launchCatching(
            block = { repo.loginUser(email, password) },
            onSuccess = { user ->
                user?.let {
                    _state.value = _state.value.copy(
                        currentUser = it,
                        isLoading = false
                    )
                } ?: run {
                    _state.value = _state.value.copy(
                        error = "Invalid email or password",
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
                    users = _state.value.users.map { if (it.id == id) updated else it },
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
                    _state.value = _state.value.copy(
                        users = _state.value.users.filterNot { it.id == id },
                        currentUser = if (_state.value.currentUser?.id == id) null else _state.value.currentUser
                    )
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