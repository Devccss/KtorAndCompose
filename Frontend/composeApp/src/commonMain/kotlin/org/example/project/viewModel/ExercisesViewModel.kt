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
import org.example.project.dtos.DifficultyLevel
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.UnitDto
import org.example.project.repository.ExerciseRepo

data class ExercisesUiState(
    val actualUnit: UnitDto,
    val selectedExercise: ExerciseDto? = null,
    val exercise: List<ExerciseDto> = emptyList(),
    var error: String? = null,
    val isLoading: Boolean = false,
)

class ExercisesViewModel(private val repo: ExerciseRepo, private val unitId:Int? = null, private val exerciseId:Int? = null ) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(
        ExercisesUiState(
            actualUnit = UnitDto(0,DifficultyLevel.A1,"","",0,false,"",),
            exercise = emptyList(),
        )
    )
    val state: StateFlow<ExercisesUiState> = _state

    private var generalMessage by mutableStateOf<String?>(null)

    fun updateMessage(message: String?) {
        generalMessage = message
    }

    init {
        if (unitId != null && exerciseId != null) {
            launchCatching(
                block = { repo.getExerciseById(exerciseId) },
                onSuccess = { exercise ->
                    _state.value = _state.value.copy(selectedExercise = exercise)
                },
                onError = { error ->
                    _state.value = _state.value.copy(error = error.message, selectedExercise = null)
                }
            )

        }else if(unitId != null && exerciseId == null){
            launchCatching(
                block = { repo.getExercisesByUnitId(unitId)},
                onSuccess = { exercise ->

                    _state.value = _state.value.copy(exercise = exercise)
                },
                onError = { error ->
                    _state.value = _state.value.copy(error = error.message, exercise = emptyList())
                }
            )
        }else{
            launchCatching(
                block = { repo.getAllExercises()},
                onSuccess = { exercise ->

                    _state.value = _state.value.copy(exercise = exercise)
                },
                onError = { error ->
                    _state.value = _state.value.copy(error = error.message, exercise = emptyList())
                }
            )
        }
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