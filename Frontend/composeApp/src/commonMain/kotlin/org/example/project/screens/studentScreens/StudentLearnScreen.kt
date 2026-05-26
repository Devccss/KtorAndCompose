package org.example.project.screens.studentScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.UnitReviewStatusDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.FilterTestsDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.TestCompletedDto
import org.example.project.dtos.TestDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UserViewModel



class StudentLearnScreen(
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo, autoLoad = false) }
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val testVm = rememberScreenModel { TestViewModel(RepositoryProvider.testRepo, RepositoryProvider.welcomeTestRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }

        val unitUi by unitVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()
        val testUi by testVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }
        var selectedIndex by remember { mutableStateOf(1) }

        LaunchedEffect(userId) {
            unitVm.searchUnits(FilterUnitsDto(isActive = true))
            exercisesVm.searchExercises(FilterExercisesDto(isActive = true))
            testVm.searchTests(FilterTestsDto(isActive = true))
            testVm.getAllExercisesInTests(onlyActiveTests = true, onlyActiveExercises = true)
            if (userId != null && userId > 0) {
                userVm.getUserById(userId)
                unitVm.searchUnits(FilterUnitsDto(isActive = true))
                testVm.getTestsCompletedByUser(userId)
                exercisesVm.getExercisesCompletedByUserId(userId)
            }
        }

        LaunchedEffect(unitUi.error, exercisesUi.error) {
            val error = unitUi.error ?: exercisesUi.error
            error?.let {
                println("Error en StudentLearnScreen: $it")
                snackbarHostState.showSnackbar(it)
            }
        }

        val unitProgress = remember(unitUi.units, exercisesUi.exercises, exercisesUi.completedExercises) {
            buildUnitProgress(
                units = unitUi.units.filter { it.isActive },
                allExercises = exercisesUi.exercises.filter { it.isActive },
                completedExerciseIds = exercisesUi.completedExercises.map { it.exerciseId }.toSet()
            )
        }

        val completedUnitIds = remember(unitUi.unitsCompleted) {
            unitUi.unitsCompleted.mapNotNull { it.id }.toSet()
        }

        // Cache of review status per unit (fetched from backend)
        val reviewStatusByUnit = remember { mutableStateMapOf<Int, UnitReviewStatusDto>() }

        LaunchedEffect(userId, unitUi.units, testUi.allTests) {
            val safeUser = userId ?: return@LaunchedEffect
            reviewStatusByUnit.clear()
            // For each unit that has a test, fetch review-status from backend
            unitUi.units.forEach { unit ->
                val uid = unit.id ?: return@forEach
                testVm.searchTests(
                    FilterTestsDto(
                        isActive = true,
                        unitId = uid
                    )
                )
                val tId = testUi.testUnit?.id ?: return@forEach
                try {
                    val status = RepositoryProvider.testRepo.getUnitReviewStatus(safeUser, uid, tId)
                    reviewStatusByUnit[uid] = status
                } catch (e: Exception) {
                    println("Error cargando reviewStatus para unidad $uid: ${e.message}")
                }
            }
        }

        val currentUnitId = userUi.currentUser?.currentUnitId ?: UserSession.actualUnit ?: unitProgress.firstOrNull()?.unit?.id
        val unlockedUnitIds = remember(unitProgress, currentUnitId, completedUnitIds) {
            if (unitProgress.isEmpty()) return@remember emptySet()

            val firstOrder = unitProgress.first().unit.orderUnit
            val currentOrder = unitProgress.firstOrNull { it.unit.id == currentUnitId }?.unit?.orderUnit
            val maxCompletedOrder = unitProgress
                .filter { it.unit.id in completedUnitIds }
                .maxOfOrNull { it.unit.orderUnit }

            val highestUnlockedOrder = listOfNotNull(
                currentOrder,
                maxCompletedOrder?.plus(1),
                firstOrder
            ).maxOrNull() ?: firstOrder

            unitProgress
                .filter { it.unit.orderUnit <= highestUnlockedOrder }
                .mapNotNull { it.unit.id }
                .toSet()
        }

        val testsByUnitId = remember(testUi.allTests) {
            testUi.allTests.filter { it.isActive }.groupBy { it.unitId }
        }

        StudentAppLayout(
            actualScreen = "Aprender",
            selectedIndex = selectedIndex,
            initialUserName = studentName,
            snackbarHostState = snackbarHostState,
            onSelect = { idx -> selectedIndex = idx },
        ) { _, _, _ ->
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Unidades",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selecciona una unidad para ver sus ejercicios reales y resolverlos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    if (unitProgress.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No hay unidades disponibles",
                                description = "Cuando existan unidades activas aparecerán aquí."
                            )
                        }
                    } else {
                                    items(unitProgress, key = { it.unit.id ?: -1 }) { progress ->
                            val unitId = progress.unit.id ?: return@items
                            val unitTest = testsByUnitId[unitId]?.firstOrNull()
                            val isUnitLocked = unitId !in unlockedUnitIds
                            val isUnitCompleted = unitId in completedUnitIds
                            val latestAttempt = unitTest?.let { testVm.getLastAttemptForTest(it.id) }
                            val unitExercisesCount = progress.totalCount
                                        val status = reviewStatusByUnit[unitId]
                                        val requiresReview = status?.requiresReview ?: false
                                        val remainingReview = status?.remainingExercises ?: 0
                                        val isTestLocked = unitTest != null && (!isUnitCompleted || (requiresReview && remainingReview > 0))

                            UnitCard(
                                progress = progress,
                                isLocked = isUnitLocked,
                                onClick = {
                                    if (isUnitLocked) return@UnitCard
                                    navigator.push(
                                        StudentUnitExercisesScreen(
                                            unitId = unitId,
                                            unitName = progress.unit.name,
                                            userIdArg = userId,
                                            studentNameArg = studentName
                                        )
                                    )
                                }
                            )

                            unitTest?.let { test ->
                                UnitTestCard(
                                    test = test,
                                    isLocked = isTestLocked,
                                    latestAttempt = latestAttempt,
                                    remainingReviewExercises = remainingReview,
                                    onClick = {
                                        if (isTestLocked) return@UnitTestCard
                                        navigator.push(
                                            StudentTestResolverScreen(
                                                testId = test.id,
                                                testName = test.name,
                                                unitId = unitId,
                                                unitName = progress.unit.name,
                                                userIdArg = userId,
                                                studentNameArg = studentName
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


