package org.example.project.screens.studentScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.StudentAppLayout
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.LearningDashboardViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel


class StudentLearnScreen(
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val dashboardVm = rememberScreenModel {
            LearningDashboardViewModel(
                RepositoryProvider.learningDashboardRepo
            )
        }
        val unitVm = rememberScreenModel {
            UnitViewModel(
                RepositoryProvider.unitRepo
            )
        }
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                welcomeTestRepo = RepositoryProvider.welcomeTestRepo
            )
        }

        val ui by dashboardVm.state.collectAsState()
        val uiUnit by unitVm.state.collectAsState()
        val uiTest by testVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }
        var selectedIndex by remember { mutableStateOf(1) }

        LaunchedEffect(Unit) {
            dashboardVm.loadDashboard()
        }


        val dashboard = ui.dashboard

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

                    if (dashboard?.units?.isEmpty() ?: true) {
                        item {
                            EmptyStateCard(
                                title = "No hay unidades disponibles",
                                description = "Cuando existan unidades activas aparecerán aquí."
                            )
                        }
                    } else {

                        //Como prosigo aqui?
                        items(dashboard.units, key = { it.unitId }) { unidad ->
                            unitVm.getUnitById(unidad.unitId)
                            testVm.getTestByUnitId(unidad.unitId)
                            testVm.fetchUnitReviewStatus(
                                userId = userId,
                                unitId = unidad.unitId,
                                testId = uiTest.currentTest?.id
                            )
                            uiUnit.actualUnit?.let {
                                UnitCard(
                                    unit = it,
                                    progressPercentage = unidad.progressPercentage,
                                    completedExercises = unidad.completedExercises,
                                    totalExercises = unidad.totalExercises,
                                    isLocked = !unidad.unlocked,
                                    onClick = {
                                        if (!unidad.unlocked) return@UnitCard

                                        navigator.push(
                                            StudentUnitExercisesScreen(
                                                unitId = unidad.unitId,
                                                unitName = unidad.unitName,
                                                userIdArg = userId,
                                                studentNameArg = studentName
                                            )
                                        )
                                    }
                                )
                            }?: unitVm.updateMessage("Error al cargar la unidad ${unidad.unitName}")

                            if (uiTest.currentTest != null) {

                                UnitTestCard(
                                    testName = uiTest.currentTest?.name ?: "Test desconocido",
                                    isLocked = unidad.testLocked,
                                    remainingReviewExercises = uiTest.reviewStatus?.remainingExercises
                                        ?: 0,
                                    requiresReview = uiTest.reviewStatus?.requiresReview ?: false,
                                    latestAttempt = uiTest.reviewStatus?.lastAttemptScore ?: 0,
                                    onClick = {

                                        if (unidad.testLocked) return@UnitTestCard

                                        navigator.push(
                                            StudentTestResolverScreen(
                                                testId = uiTest.currentTest?.id ?: -1,
                                                testName = uiTest.currentTest?.name ?: "Test desconocido",
                                                unitId = unidad.unitId,
                                                unitName = unidad.unitName,
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


