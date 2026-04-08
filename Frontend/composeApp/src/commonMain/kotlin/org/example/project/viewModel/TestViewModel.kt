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
import org.example.project.dtos.CreateWelcomeTestDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.TestExerciseDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateTestExerciseDto
import org.example.project.dtos.UpdateWelcomeTestDto
import org.example.project.dtos.WelcomeTestDto
import org.example.project.repository.TestRepo
import org.example.project.repository.WelcomeTestRepo

data class TestUIState(
    val selectedTests: List<TestDto> = emptyList(),
    val allTests: List<TestDto> = emptyList(),
    val currentTest: TestDto? = null,
    val testExercises: List<ExerciseDto> = emptyList(),
    val welcomeTests: List<WelcomeTestDto> = emptyList(),
    val currentWelcomeTest : TestDto? = null,
    var error: String? = null,
    val isLoading: Boolean = false,
)

class TestViewModel(
    private val testRepo:TestRepo,
    private val welcomeTestRepo: WelcomeTestRepo
):ViewModel(),ScreenModel {
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
                _state.value = _state.value.copy(allTests = tests)
            },
            onError = { error ->
                _state.value = _state.value.copy(
                    error = "Error al obtener todos los tests: ${error.message}",
                    selectedTests = emptyList(),
                    allTests = emptyList()
                )
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
                _state.value = _state.value.copy(allTests = _state.value.allTests + newTest)
                getAllTests()
            },
            onError = { error ->
                _state.value =
                    _state.value.copy(error = "${error.message}", currentTest = null)
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

    //WelcomeTest
    fun getAllWelcomeTests() {
        launchCatching(
            block = { welcomeTestRepo.getAllWelcomeTests() },
            onSuccess = { welcomeTests ->
                _state.value = _state.value.copy(welcomeTests = welcomeTests)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener los welcome tests: ${error.message}", welcomeTests = emptyList())
            }
        )
    }

    fun getAllTestsFromWelcomeTests() {
        launchCatching(
            block = { welcomeTestRepo.getAllTestsFromWelcomeTests() },
            onSuccess = { tests ->
                _state.value = _state.value.copy(selectedTests = tests)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener los tests de welcome tests: ${error.message}", selectedTests = emptyList())
            }
        )
    }

    fun getWelcomeTestByTestId(testId: Int) {
        launchCatching(
            block = { welcomeTestRepo.getWelcomeTestById(testId) },
            onSuccess = { welcomeTest ->
                _state.value = _state.value.copy(currentWelcomeTest = welcomeTest )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener el welcome test por testId: ${error.message}", welcomeTests = emptyList())
            }
        )
    }

    fun createWelcomeTest(dto: CreateWelcomeTestDto) {
        launchCatching(
            block = { welcomeTestRepo.createWelcomeTest(dto) },
            onSuccess = {
                getAllWelcomeTests()
                getAllTests()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al crear el welcome test: ${error.message}")
            }
        )
    }

    fun updateWelcomeTest(testId: Int, dto: UpdateWelcomeTestDto) {
        launchCatching(
            block = { welcomeTestRepo.updateWelcomeTest(testId, dto) },
            onSuccess = { success ->
                if (success) {
                    getAllWelcomeTests()
                } else {
                    _state.value = _state.value.copy(error = "Error al actualizar el welcome test: No se pudo actualizar")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al actualizar el welcome test: ${error.message}")
            }
        )
    }
    fun deleteWelcomeTest(testId: Int) {
        launchCatching(
            block = {
                val relationDeleted = welcomeTestRepo.deleteWelcomeTest(testId)
                if (!relationDeleted) return@launchCatching false

                testRepo.deleteTest(testId)
             },
             onSuccess = { success ->
                 if (success) {
                     getAllWelcomeTests()
                     getAllTests()
                     getAllTestsFromWelcomeTests()
                 } else {
                    _state.value = _state.value.copy(error = "Error al eliminar el welcome test y su test asociado")
                 }
             },
             onError = { error ->
                _state.value = _state.value.copy(error = "Error al eliminar el welcome test y su test asociado: ${error.message}")
             }
         )
     }

    fun setWelcomeTest(testId: Int) {
        launchCatching(
            block = {
                val relations = welcomeTestRepo.getAllWelcomeTests()
                if (relations.none { it.testId == testId }) {
                    welcomeTestRepo.createWelcomeTest(CreateWelcomeTestDto(testId = testId, isActive = false))
                }
                true
            },
            onSuccess = {
                getAllWelcomeTests()
                getAllTests()
                getAllTestsFromWelcomeTests()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al asociar welcome test: ${error.message}")
            }
        )
    }

    fun activateWelcomeTest(testId: Int) {
        launchCatching(
            block = {
                val relations = welcomeTestRepo.getAllWelcomeTests()
                if (relations.none { it.testId == testId }) {
                    welcomeTestRepo.createWelcomeTest(CreateWelcomeTestDto(testId = testId, isActive = false))
                }
                setSingleActiveWelcomeTest(testId)
                true
            },
            onSuccess = {
                getAllWelcomeTests()
                getAllTestsFromWelcomeTests()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al activar welcome test: ${error.message}")
            }
        )
    }

    fun createAndSetWelcomeTest(
        dto: CreateTestDto,
        exerciseIds: List<Int>
    ) {
        launchCatching(
            block = {
                val createdTest = welcomeTestRepo.createTestForWelcomeTest(dto)
                exerciseIds.distinct().forEach { exerciseId ->
                    testRepo.createTestExercise(
                        CreateTestExerciseDto(
                            testId = createdTest.id,
                            exerciseId = exerciseId
                        )
                    )
                }

                welcomeTestRepo.createWelcomeTest(
                    CreateWelcomeTestDto(testId = createdTest.id, isActive = false)
                )
                createdTest
            },
            onSuccess = { createdTest ->
                _state.value = _state.value.copy(selectedTests = _state.value.selectedTests + createdTest)
                getAllWelcomeTests()
                getAllTests()
                getAllTestsFromWelcomeTests()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al crear y asociar welcome test: ${error.message}")
            }
        )
    }

    private suspend fun setSingleActiveWelcomeTest(activeTestId: Int) {
        val relations = welcomeTestRepo.getAllWelcomeTests()
        relations.forEach { relation ->
            val shouldBeActive = relation.testId == activeTestId
            if (relation.isActive != shouldBeActive) {
                welcomeTestRepo.updateWelcomeTest(
                    relation.testId,
                    UpdateWelcomeTestDto(isActive = shouldBeActive)
                )
            }
        }
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