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
import org.example.project.NotificationData
import org.example.project.dtos.CreateExerciseContentDto
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.CreateExerciseCompletedDto
import org.example.project.dtos.ExerciseCompletedDto
import org.example.project.dtos.ExerciseContentDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateExerciseContentDto
import org.example.project.dtos.UpdateExerciseDto
import org.example.project.repository.ExerciseRepo
data class ExercisesUiState(
    val actualUnit: UnitDto? = null,
    val selectedExercise: ExerciseDto? = null,
    val selectedContent : ExerciseContentDto? = null,
    val contents : List<ExerciseContentDto> = emptyList(),
    val completedExercises: List<ExerciseCompletedDto> = emptyList(),
    val exercises: List<ExerciseDto> = emptyList(),
    var error: String? = null,
    val isLoading: Boolean = false,
)

class ExercisesViewModel(private val repo: ExerciseRepo, private val unitId:Int? = null, exerciseId:Int? = null ) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(
        ExercisesUiState(
            isLoading = true
        )
    )
    val state: StateFlow<ExercisesUiState> = _state

    private var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        generalMessage = message
            _state.value = _state.value.copy(error = message)
        println("Mensaje actualizado: $message")
    }

    fun searchExercises(filters: FilterExercisesDto){
        val normalized = filters.copy(name = filters.name?.trim()?.takeIf { it.isNotEmpty() })
        val hasRealFilter = normalized.name != null || normalized.isActive != null
        launchCatching(
            block = {
                if (unitId != null) {
                    // En modo unidad: si no hay filtros reales, evitar /search y cargar por endpoint directo.
                    if (!hasRealFilter) {
                        repo.getExercisesByUnitId(unitId)
                    } else {
                        val newFilter = normalized.copy(unitId = unitId)
                        repo.searchExercises(newFilter)
                    }
                }else{
                    // En modo global: si no hay filtros reales, usar endpoint directo de todos.
                    if (!hasRealFilter && normalized.unitId == null) {
                        repo.getAllExercises()
                    } else {
                        repo.searchExercises(normalized)
                    }
                }

            },
            onSuccess = { exercise ->
                NotificationData.totalExercises = exercise.size
                _state.value = _state.value.copy(exercises = exercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al buscar ejercicios: ${error.message}", exercises = emptyList())
            }
        )
    }

    fun getAllExercises() {
        launchCatching(
            block = { repo.getAllExercises()},
            onSuccess = { exercises ->
                _state.value = _state.value.copy(exercises = exercises)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener todos los ejercicios: ${error.message}", exercises = emptyList())
            }
        )
    }

    fun getExerciseById(exerciseId: Int) {
        launchCatching(
            block = { repo.getExerciseById(exerciseId) },
            onSuccess = { exercise ->
                getContentByExerciseId(exerciseId)
                _state.value = _state.value.copy(selectedExercise = exercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener ejercicio por id: ${error.message}", selectedExercise = null)
            }
        )
    }

    fun getExercisesByUnitId(unitId: Int) {
        launchCatching(
            block = { repo.getExercisesByUnitId(unitId)},
            onSuccess = { exercises ->
                _state.value = _state.value.copy(exercises = exercises)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener ejercicios por unidad: ${error.message}", exercises = emptyList())
            }
        )
    }

    fun createExercise(dto: CreateExerciseDto) {
        launchCatching(
            block = { repo.createExercise(dto) },
            onSuccess = { newExercise ->
                val currentList = _state.value.exercises
                _state.value = _state.value.copy(exercises = currentList + newExercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al crear ejercicio: ${error.message}")
            }
        )
    }

    fun updateExercise(exerciseId: Int, dto: UpdateExerciseDto) {
        launchCatching(
            block = { repo.updateExercise(exerciseId, dto) },
            onSuccess = {
                getExerciseById(exerciseId)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al actualizar ejercicio: ${error.message}")
            }
        )
    }

    fun deleteExercise(exerciseId: Int) {
        launchCatching(
            block = { repo.deleteExercise(exerciseId) },
            onSuccess = {
                val currentList = _state.value.exercises
                _state.value = _state.value.copy(exercises = currentList.filterNot { it.id == exerciseId })
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al eliminar ejercicio: ${error.message}")
            }
        )
    }

    fun reorderExercises(updates: List<Pair<Int, Int>>) {
        launchCatching(
            block = { repo.reorderExercises(updates) },
            onSuccess = {

                if (unitId != null) {
                    getExercisesByUnitId(unitId)
                } else {
                    getAllExercises()
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al reordenar: ${error.message}")
            }
        )
    }


    //Content Exercise

    fun getAllContentExercises() {
        launchCatching(
            block = { repo.getAllExerciseContent()},
            onSuccess = { content ->

                _state.value = _state.value.copy(contents = content)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener todos los ejercicios: ${error.message}", exercises = emptyList())
            }
        )
    }
    fun getContentByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { repo.getExerciseContentByExerciseId(exerciseId) },
            onSuccess = { content ->
                content?.let {
                    _state.value = _state.value.copy(selectedContent = it)
                }?: run {
                    _state.value = _state.value.copy( selectedContent = null)
                }
            },
            onError = { error ->
                if (error.cause is Throwable){
                    _state.value = _state.value.copy(selectedContent = null)
                    return@launchCatching
                }
                _state.value = _state.value.copy(error = "Error al obtener contenido por id del ejercicio: ${error.message}", selectedContent = null)
            }
        )
    }
    fun createContent(exerciseId: Int, dto: CreateExerciseContentDto) {
        launchCatching(
            block = { repo.createExerciseContent(exerciseId, dto) },
            onSuccess = { newContent ->
                _state.value = _state.value.copy(selectedContent = newContent)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al crear contenido: ${error.message}")
            }
        )
    }

    fun updateContent(exerciseId: Int, dto: UpdateExerciseContentDto) {
        launchCatching(
            block = { repo.updateExerciseContent( exerciseId, dto) },
            onSuccess = {
                getContentByExerciseId(exerciseId)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al actualizar contenido: ${error.message}")
            }
        )
    }

    fun deleteContent(exerciseId: Int) {
        launchCatching(
            block = { repo.deleteExerciseContent(exerciseId) },
            onSuccess = {
                _state.value = _state.value.copy(selectedContent = null)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al eliminar contenido: ${error.message}")
            }
        )
    }

    // ExercisesCompleted
    fun getExercisesCompletedByUserId(userId: Int) {
        launchCatching(
            block = { repo.getExercisesCompletedByUserId(userId) },
            onSuccess = { completed ->
                NotificationData.completedExercises = completed.size
                _state.value = _state.value.copy(completedExercises = completed)
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = "Error al obtener ejercicios completados del usuario: ${error.message}",
                    completedExercises = emptyList()
                )
            }
        )
    }

    fun createExerciseCompleted(dto: CreateExerciseCompletedDto) {
        launchCatching(
            block = { repo.createExerciseCompleted(dto) },
            onSuccess = { completed ->
                _state.value = _state.value.copy(
                    completedExercises = _state.value.completedExercises + completed
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = "Error al crear ejercicio completado: ${error.message}"
                )
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