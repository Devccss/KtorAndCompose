package org.example.project.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTestCompletedDto
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateTestExerciseDto
import org.example.project.dtos.CreateWelcomeTestDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterTestsDto
import org.example.project.dtos.TestCompletedDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitReviewStatusDto
import org.example.project.dtos.UpdateTestCompletedDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateTestExerciseDto
import org.example.project.dtos.UpdateWelcomeTestDto
import org.example.project.dtos.WelcomeTestDto
import org.example.project.network.UserSession
import org.example.project.repository.TestRepo
import org.example.project.repository.WelcomeTestRepo

data class TestUIState(
    val selectedTests: List<TestDto> = emptyList(),
    val allTests: List<TestDto> = emptyList(),
    val currentTest: TestDto? = null,
    val searchTest: List<TestDto> = emptyList(),
    val testUnit: TestDto? = null,
    val testExercises: List<ExerciseDto> = emptyList(),
    val allTestExercises: Map<Int, List<ExerciseDto>> = emptyMap(),  // testId -> ejercicios
    val allExercisesInTests: Set<Int> = emptySet(),  // IDs de ejercicios que están en algún test
    val reviewStatus : UnitReviewStatusDto? = null,
    val welcomeTests: List<WelcomeTestDto> = emptyList(),
    val currentWelcomeTest : TestDto? = null,
    val testsCompleted: List<TestCompletedDto> = emptyList(),
    val currentTestCompleted: TestCompletedDto? = null,

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

    fun getLastAttemptForTest(testId: Int): TestCompletedDto? =
        _state.value.testsCompleted
            .filter { it.testId == testId }
            .maxByOrNull { it.id }

    fun hasPassedTest(testId: Int): Boolean = getLastAttemptForTest(testId)?.score == 100

    @Deprecated("La verificación de repaso ahora se hace en el backend. Usar fetchUnitReviewStatus")
    fun requiresReview(unitId: Int, completedUnitsCount: Int): Boolean =
        UserSession.requiresUnitReview(unitId, completedUnitsCount)

    fun markTestRequiresReview(unitId: Int, completedUnitsCount: Int) {
        UserSession.markUnitRequiresReview(unitId, completedUnitsCount)
    }

    fun clearTestReviewRequirement(unitId: Int) {
        UserSession.clearUnitReviewRequirement(unitId)
    }

    @Deprecated("Usar fetchUnitReviewStatus para obtener el estado real desde el backend")
    fun remainingReviewExercises(unitId: Int, totalExercisesInUnit: Int): Int {
        val required = UserSession.requiredReviewExercises(totalExercisesInUnit)
        val done = UserSession.reviewedExercisesCount(unitId)
        return (required - done).coerceAtLeast(0)
    }

    fun registerReviewedExercise(unitId: Int, exerciseId: Int) {
        // Legacy: the backend now computes review progress. Keep this method as a no-op for compatibility.
        // Prefer creating ExerciseCompleted via ExerciseViewModel / ExerciseRepo so the server can count it.
        UserSession.registerReviewedExercise(unitId, exerciseId)
    }

    // Nuevo: consultar al backend el estado de repaso para una unidad/test y usuario
    fun fetchUnitReviewStatus(userId: Int? , unitId: Int?, testId: Int?){
        if (userId == null || unitId == null || testId == null) {
            _state.value = _state.value.copy(error = "No se puede obtener estado de repaso: userId, unitId o testId es nulo", reviewStatus = null)
            return
        }
        launchCatching(
            block = { testRepo.getUnitReviewStatus(userId, unitId, testId) },
            onSuccess = { status ->
                _state.value = _state.value.copy(reviewStatus = status)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener estado de repaso: ${error.message}", reviewStatus = null)
            }
        )
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

    fun getTestByUnitId(unitId: Int) {
        launchCatching(
            block = { testRepo.getTestByUnitId(unitId) },
            onSuccess = { test ->
                println("Test obtenido por unitId $unitId: $test")
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

        fun searchTests(filters: FilterTestsDto) {
            val normalized = filters.copy(name = filters.name?.trim()?.takeIf { it.isNotEmpty() })
            val hasFilters = normalized.name != null || normalized.unitId != null || normalized.isActive != null
            launchCatching(
                block = {
                    if (hasFilters) testRepo.searchTests(normalized)
                    else testRepo.getAllTests()
                },
                onSuccess = { tests ->
                    _state.value = _state.value.copy(searchTest = tests, allTests = tests)
                },
                onError = { error ->
                    _state.value =
                        _state.value.copy(error = "Error al buscar tests: ${error.message}", searchTest = emptyList())
                }
            )
        }


    //TesExercise

    fun getExercisesByTestId(testId: Int) {
        launchCatching(
            block = { testRepo.getExercisesByTestId(testId) },
            onSuccess = { exercises ->
                val activeExercises = exercises.filter { it.isActive }
                _state.value = _state.value.copy(testExercises = activeExercises)
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
                if (!success) {
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
                val activeTests = tests.filter { it.isActive }
                _state.value = _state.value.copy(selectedTests = activeTests)
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

    //TestCompleted
    fun getAllTestCompleted() {
        launchCatching(
            block = { testRepo.getAllTestCompleted() },
            onSuccess = { testCompletedList ->
                _state.value = _state.value.copy(testsCompleted = testCompletedList)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener los tests completados: ${error.message}")
            }
        )
    }

    fun getTestCompletedById(id: Int) {
        launchCatching(
            block = { testRepo.getTestCompletedById(id) },
            onSuccess = { testCompleted ->
                _state.value = _state.value.copy(currentTestCompleted = testCompleted)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener el test completado por ID: ${error.message}")
            }
        )
    }

    fun getTestsCompletedByUser(userId: Int) {
        launchCatching(
            block = { testRepo.getTestsCompletedByUser(userId) },
            onSuccess = { testsCompleted ->
                _state.value = _state.value.copy(testsCompleted = testsCompleted)
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener los tests completados por usuario: ${error.message}")
            }
        )
    }

    fun createTestCompleted(dto: CreateTestCompletedDto) {
        launchCatching(
            block = { testRepo.createTestCompleted(dto) },
            onSuccess = { newTestCompleted ->
                _state.value = _state.value.copy(
                    testsCompleted = _state.value.testsCompleted + newTestCompleted,
                    currentTestCompleted = newTestCompleted
                )
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al crear el test completado: ${error.message}")
            }
        )
    }

    fun updateTestCompleted(id: Int, dto: UpdateTestCompletedDto) {
        launchCatching(
            block = { testRepo.updateTestCompleted(id, dto) },
            onSuccess = {
                getTestCompletedById(id)
                getAllTestCompleted()
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al actualizar el test completado: ${error.message}")
            }
        )
    }

    fun deleteTestCompleted(id: Int) {
        launchCatching(
            block = { testRepo.deleteTestCompleted(id) },
            onSuccess = { success ->
                if (success) {
                    _state.value = _state.value.copy(currentTestCompleted = null)
                    getAllTestCompleted()
                } else {
                    _state.value = _state.value.copy(error = "Error al eliminar el test completado: Respuesta no exitosa")
                }
            },
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al eliminar el test completado: ${error.message}")
            }
        )
    }

    // Obtener todos los ejercicios que están en tests
    fun getAllExercisesInTests(
        onlyActiveTests: Boolean = false,
        onlyActiveExercises: Boolean = false
    ) {
        launchCatching(
            block = {
                // Primero obtener todos los tests
                val allTests = testRepo.searchTests( FilterTestsDto(isActive =  true) )
                val sourceTests = if (onlyActiveTests) allTests.filter { it.isActive } else allTests
                // Luego para cada test, obtener sus ejercicios
                val testExercisesMap = mutableMapOf<Int, List<ExerciseDto>>()
                val allExerciseIds = mutableSetOf<Int>()

                sourceTests.forEach { test ->
                    val rawExercises = testRepo.getExercisesByTestId(test.id)
                    val exercises = if (onlyActiveExercises) rawExercises.filter { it.isActive } else rawExercises
                    testExercisesMap[test.id] = exercises
                    allExerciseIds.addAll(exercises.map { it.id })
                }

                _state.value = _state.value.copy(
                    allTestExercises = testExercisesMap,
                    allExercisesInTests = allExerciseIds
                )
            },
            onSuccess = {},
            onError = { error ->
                _state.value = _state.value.copy(error = "Error al obtener ejercicios en tests: ${error.message}")
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