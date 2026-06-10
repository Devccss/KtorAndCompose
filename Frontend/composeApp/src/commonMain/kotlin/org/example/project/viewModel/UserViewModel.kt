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
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUsersDto
import org.example.project.dtos.GeneralStatsDto
import org.example.project.dtos.LoginDto
import org.example.project.dtos.Role
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUserDto
import org.example.project.dtos.UserDto
import org.example.project.dtos.UserStatsDto
import org.example.project.dtos.StudentsStatsSummaryDto
import org.example.project.network.UserSession
import org.example.project.repository.UnitRepo
import org.example.project.repository.UserRepo


data class UsersUiState(
    val users: List<UserDto> = emptyList(),
    val unit: List<UnitDto> = emptyList(),
    val currentUser: UserDto? = null,
    val userStats: UserStatsDto? = null,
    val completedUnits: List<UnitDto> = emptyList(),
    val completedExercises: List<ExerciseDto> = emptyList(),
    val completedTests: List<TestDto> = emptyList(),
    val failedTests: List<TestDto> = emptyList(),
    val weeklyHours: Double = 0.0,
    val sessionCount: Int = 0,
    val allUserStats : GeneralStatsDto? = null,
    val studentsStats: StudentsStatsSummaryDto? = null,
    val studentsStatsLoading: Boolean = false,
    val studentsStatsError: String? = null,
    val statsLoading: Boolean = false,
    val statsError: String? = null,
    val isLoading: Boolean = false,
    val registerUser: CreateUserDto? = null,
    var error: String? = null,

    )

@Suppress("unused")
class UserViewModel(private val repo: UserRepo, private val unitRepo: UnitRepo) : ViewModel(),
    ScreenModel {
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
        // Referenciar unitRepo de forma inocua para evitar advertencias de "propiedad no utilizada"
        // (esto no altera la lógica de la clase)
        unitRepo.hashCode()

        launchCatching(
            block = { return@launchCatching true },
            onSuccess = {
                _state.value = _state.value.copy(
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

    fun loadUsers() {
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
        // Normalizar los filtros: trim del nombre, ignorar role vacío, aceptar unitId > 0
        val name = filters.name?.trim()?.takeIf { it.isNotEmpty() }
        var role = filters.role?.takeIf { true }
        val unitId = filters.unitId?.takeIf { it > 0 }

        // Si no se proporcionaron filtros efectivos, cargar todos los usuarios en lugar de hacer una consulta vacía
        if (name == null && role == null && unitId == null) {
            role = Role.STUDENT
        }

        val normalized = filters.copy(
            name = name,
            role = role,
            unitId = unitId
        )
        launchCatching(
            block = {
                repo.getFilterUsers(normalized)
            },
            onSuccess = { users ->
                if (users.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        users = users,
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "No se encontraron usuarios con esos filtros",
                        users = emptyList()
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

    fun getUserById(id: Int) {
        if (id <= 0) {
            _state.value = _state.value.copy(error = "Invalid user id: $id")
            return
        }

        launchCatching(
            block = { repo.getUserById(id) },
            onSuccess = { user ->
                if (user != null) {

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
    fun loadAllUsersStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(statsLoading = true, statsError = null)
            try {
                val generalStats = repo.getAllUserStats()
                _state.value = _state.value.copy(
                    allUserStats = generalStats,
                    statsLoading = false,
                    statsError = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    statsLoading = false,
                    statsError = e.message ?: "No se pudieron cargar las estadísticas"
                )
            }
        }
    }
    fun loadUserStatistics(userId: Int) {
        if (userId <= 0) {
            _state.value = _state.value.copy(
                statsError = "Invalid user id: $userId",
                userStats = null,
                completedUnits = emptyList(),
                completedExercises = emptyList(),
                completedTests = emptyList(),
                failedTests = emptyList(),
                weeklyHours = 0.0,
                sessionCount = 0,
                statsLoading = false
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(statsLoading = true, statsError = null)
            try {
                val summary = runCatching { repo.getUserStats(userId) }.getOrNull()
                val completedUnits = runCatching { repo.getCompletedUnitsByUserId(userId) }.getOrDefault(emptyList())
                val completedExercises = runCatching { repo.getCompletedExercisesByUserId(userId) }.getOrDefault(emptyList())
                val completedTests = runCatching { repo.getCompletedTestsByUserId(userId) }.getOrDefault(emptyList())
                val failedTests = runCatching { repo.getFailedTestsByUserId(userId) }.getOrDefault(emptyList())
                val weeklyHours = runCatching { repo.getWeeklyHoursByUserId(userId) }.getOrNull()

                _state.value = _state.value.copy(
                    userStats = summary,
                    completedUnits = completedUnits,
                    completedExercises = completedExercises,
                    completedTests = completedTests,
                    failedTests = failedTests,
                    weeklyHours = weeklyHours?.weeklyHours ?: summary?.weeklyHours ?: 0.0,
                    sessionCount = weeklyHours?.sessionCount ?: 0,
                    statsLoading = false,
                    statsError = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    statsLoading = false,
                    statsError = e.message ?: "No se pudieron cargar las estadísticas"
                )
            }
        }
    }

    fun loadStudentsStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(studentsStatsLoading = true, studentsStatsError = null)
            try {
                val stats = repo.getStudentsStats()
                _state.value = _state.value.copy(
                    studentsStats = stats,
                    studentsStatsLoading = false,
                    studentsStatsError = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    studentsStatsLoading = false,
                    studentsStatsError = e.message ?: "No se pudieron cargar las estadísticas de alumnos"
                )
            }
        }
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

    fun updateUser(id: Int, updatedUser: UpdateUserDto) {
        launchCatching(
            block = { repo.updateUser(id, updatedUser) },
            onSuccess = {
                UserSession.idUser?.let { getUserById(it) }
                    ?: throw IllegalStateException("No se encontro el usuario con ID ${UserSession.idUser}")
            },
            onError = { error ->
                _state.value = _state.value.copy(error = error.message)
            }
        )
    }

    fun updateUserCurrentUnit(userId: Int, unitId: Int) {
        launchCatching(
            block = {
                repo.updateUser(userId, UpdateUserDto(currentUnitId = unitId))
            },
            onSuccess = {
                getUserById(userId)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al actualizar unidad actual: ${error.message}")
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
        UserSession.clear()
        _state.value = _state.value.copy(currentUser = null)
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val result = block()
            onSuccess(result)
        } catch (e: Exception) {
            onError(e)
            _state.value = _state.value.copy(error = e.message)
        } finally {
            _state.value = _state.value.copy(isLoading = false)

        }
    }
}