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
import org.example.project.dtos.CreateExerciseContentDto
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.DifficultyLevel
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
    val exercise: List<ExerciseDto> = emptyList(),
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

    init {
        if (unitId != null && exerciseId != null) {
            getExerciseById(exerciseId)

        }else if(unitId != null && exerciseId == null){
            getExercisesByUnitId(unitId)
        }else{
            getAllExercises()
        }
    }

    fun searchExercises(filters: FilterExercisesDto){
        launchCatching(
            block = { repo.searchExercises(filters)},
            onSuccess = { exercise ->

                _state.value = _state.value.copy(exercise = exercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al buscar ejercicios: ${error.message}", exercise = emptyList())
            }
        )
    }

    fun getAllExercises() {
        launchCatching(
            block = { repo.getAllExercises()},
            onSuccess = { exercise ->

                _state.value = _state.value.copy(exercise = exercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener todos los ejercicios: ${error.message}", exercise = emptyList())
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
            onSuccess = { exercise ->

                _state.value = _state.value.copy(exercise = exercise)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener ejercicios por unidad: ${error.message}", exercise = emptyList())
            }
        )
    }

    fun createExercise(dto: CreateExerciseDto) {
        launchCatching(
            block = { repo.createExercise(dto) },
            onSuccess = { newExercise ->
                val currentList = _state.value.exercise
                _state.value = _state.value.copy(exercise = currentList + newExercise)
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
                _state.value = _state.value.copy(error = "Error al obtener todos los ejercicios: ${error.message}", exercise = emptyList())
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