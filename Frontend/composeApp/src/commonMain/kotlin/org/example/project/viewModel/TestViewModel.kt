package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateTestExerciseDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.TestExerciseDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateTestExerciseDto
import org.example.project.repository.TestRepo

data class TestUIState(
    val selectedTests: List<TestDto> = emptyList(),
    val allTests: List<TestDto> = emptyList(),
    val currentTest: TestDto? = null,
    val testExercises: List<ExerciseDto> = emptyList(),
    var error: String? = null,
    val isLoading: Boolean = false,
)

class TestViewModel(private val testRepo:TestRepo ):ViewModel(),ScreenModel {
    private val _state = MutableStateFlow(
        TestUIState(
            selectedTests = emptyList(),
            allTests = emptyList(),
            currentTest = null
        )
    )
    val state : StateFlow<TestUIState> = _state


    fun updateMessage(message: String?){
        _state.value = _state.value.copy(error = message)
    }

    fun getAllTests(){
        launchCatching(
            block = { testRepo.getAllTests() },
            onSuccess = { tests ->
                _state.value = _state.value.copy(selectedTests = tests)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener todos los tests: ${error.message}", allTests = emptyList())
            }
        )
    }

     fun getTestById(id: Int) {
        launchCatching(
            block = { testRepo.getTestById(id) },
            onSuccess = { test ->
                _state.value = _state.value.copy(currentTest = test)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener test por ID: ${error.message}", currentTest = null)
            }
        )
    }

    fun getTestsByUnitId(unitId: Int) {
        launchCatching(
            block = { testRepo.getTestsByUnitId(unitId) },
            onSuccess = { test ->
                _state.value = _state.value.copy(currentTest = test)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener tests por unitId: ${error.message}", selectedTests = emptyList())
            }
        )
    }

    fun getTestByExerciseId(exerciseId: Int) {
        launchCatching(
            block = { testRepo.getTestByExerciseId(exerciseId) },
            onSuccess = { test ->
                _state.value = _state.value.copy(currentTest = test)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener test por exerciseId: ${error.message}", selectedTests = emptyList())
            }
        )
    }

    fun createTest(test: CreateTestDto) {
        launchCatching(
            block = { testRepo.createTest(test) },
            onSuccess = { newTest ->
                _state.value = _state.value.copy(selectedTests = _state.value.selectedTests + newTest)
                getAllTests()
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al crear test: ${error.message}", currentTest = null)
            }
        )
    }

    fun updateTest(id: Int, test: UpdateTestDto) {
        launchCatching(
            block = { testRepo.updateTest(id, test) },
            onSuccess = { success ->
                if (success) {
                    getTestById(id)
                    getAllTests()
                } else {
                    _state.value =
                        _state.value.copy(error = "Error al actualizar test: Respuesta no exitosa", currentTest = null)
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al actualizar test: ${error.message}", currentTest = null)
            }
        )
    }

    fun deleteTest(id: Int) {
        launchCatching(
            block = { testRepo.deleteTest(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(currentTest = null)
                    getAllTests()
                } else {
                    _state.value =
                        _state.value.copy(error = "Error al eliminar test: Respuesta no exitosa")
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al eliminar test: ${error.message}")
            }
        )
    }


    //TesExercise

    fun getExercisesByTestId(testId: Int) {
        launchCatching(
            block = { testRepo.getExercisesByTestId(testId) },
            onSuccess = { exercises ->
                _state.value = _state.value.copy(testExercises = exercises)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al obtener test-exercises por testId: ${error.message}")
            }
        )
    }


    fun createTestExercise(dto: CreateTestExerciseDto) {
        launchCatching(
            block = { testRepo.createTestExercise(dto) },
            onSuccess = {
                getExercisesByTestId(dto.testId)
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al crear test-exercise: ${error.message}")
            }
        )
    }

     fun updateTestExercise(id: Int, dto: UpdateTestExerciseDto) {
        launchCatching(
            block = { testRepo.updateTestExercise(id, dto) },
            onSuccess = { success ->
                if (success) { } else {
                    _state.value =
                        _state.value.copy(error = "Error al actualizar test-exercise: Respuesta no exitosa")
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al actualizar test-exercise: ${error.message}")
            }
        )
    }

    fun deleteTestExercise(exerciseId: Int) {
        launchCatching(
            block = { testRepo.deleteTestExercise(exerciseId) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(testExercises = _state.value.testExercises.filterNot { it.id == exerciseId })
                } else {
                    _state.value =
                        _state.value.copy(error = "Error al eliminar test-exercise: Respuesta no exitosa")
                }
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "Error al eliminar test-exercise: ${error.message}")
            }
        )
    }

    private fun <T> launchCatching(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val result = block()
            onSuccess(result)
            _state.update { it.copy(isLoading = false) }

        } catch (e: Exception) {
            onError(e)
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}