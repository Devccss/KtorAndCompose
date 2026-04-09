package org.example.project.screens.studentScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.CreateExerciseCompletedDto
import org.example.project.dtos.CreateTestCompletedDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.UnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class StudentWelcomeScreen(val id: Int? = null, val studentName: String? = null) : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }

        val testUi by testVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()
        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        val userId = id ?: UserSession.idUser
        val name = studentName ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(userId) {
            testVm.getAllWelcomeTests()
            exercisesVm.getAllExercises()
            unitVm.getAllUnits()

            if (userId != null && userId > 0) {
                testVm.getTestsCompletedByUser(userId)
                exercisesVm.getExercisesCompletedByUserId(userId)
                userVm.getUserById(userId)
            }
        }

        val activeWelcome = testUi.welcomeTests.firstOrNull { it.isActive }

        LaunchedEffect(activeWelcome?.testId) {
            activeWelcome?.testId?.let { testVm.getExercisesByTestId(it) }
        }

        val unitsOrdered = remember(unitUi.units) { unitUi.units.sortedBy { it.orderUnit } }
        val progress = remember(unitsOrdered, exercisesUi.exercises, exercisesUi.completedExercises) {
            estimatePlacement(
                units = unitsOrdered,
                allExercises = exercisesUi.exercises,
                completedExerciseIds = exercisesUi.completedExercises.map { it.exerciseId }.toSet()
            )
        }

        var placementSynced by remember(userId) { mutableStateOf(false) }
        LaunchedEffect(progress.currentUnitId, userId, unitsOrdered, exercisesUi.completedExercises.size) {
            val targetUnitId = progress.currentUnitId
            if (
                !placementSynced &&
                userId != null && userId > 0 &&
                targetUnitId != null &&
                targetUnitId != UserSession.actualUnit
            ) {
                userVm.updateUserCurrentUnit(userId, targetUnitId)
                UserSession.set(
                    id = userId,
                    name = UserSession.name,
                    role = UserSession.role,
                    actualUnit = targetUnitId
                )
                placementSynced = true
            }
        }

        LaunchedEffect(testUi.error, exercisesUi.error, userUi.error) {
            val error = testUi.error ?: exercisesUi.error ?: userUi.error
            error?.let { snackbarHostState.showSnackbar(it) }
        }

        val completedWelcomeTestIds = testUi.testsCompleted.map { it.testId }.toSet()
        val activeWelcomePending = activeWelcome?.testId?.let { it !in completedWelcomeTestIds } == true
        val currentUnitName = unitsOrdered.firstOrNull { it.id == (UserSession.actualUnit ?: progress.currentUnitId) }?.name

        StudentAppLayout(
            actualScreen = "Inicio",
            selectedIndex = 0,
            initialUserName = name,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->
            StudentDashboardContent(
                name = name,
                currentUnitName = currentUnitName,
                progress = progress,
                welcomeTestPending = activeWelcomePending,
                welcomeExercises = testUi.testExercises,
                completedExerciseIds = exercisesUi.completedExercises.map { it.exerciseId }.toSet(),
                isLoading = testUi.isLoading || exercisesUi.isLoading || unitUi.isLoading,
                onFinishWelcome = { resolvedIds ->
                    val safeUserId = userId ?: return@StudentDashboardContent
                    val safeTestId = activeWelcome?.testId ?: return@StudentDashboardContent

                    val now = Clock.System.now().toString()
                    val alreadyCompleted = exercisesUi.completedExercises.map { it.exerciseId }.toSet()
                    val newResolved = resolvedIds.filterNot { it in alreadyCompleted }

                    newResolved.forEach { exerciseId ->
                        exercisesVm.createExerciseCompleted(
                            CreateExerciseCompletedDto(
                                userId = safeUserId,
                                exerciseId = exerciseId,
                                completedAt = now
                            )
                        )
                    }

                    testVm.createTestCompleted(
                        CreateTestCompletedDto(
                            userId = safeUserId,
                            testId = safeTestId,
                            completedAt = now
                        )
                    )

                    testVm.getTestsCompletedByUser(safeUserId)
                    exercisesVm.getExercisesCompletedByUserId(safeUserId)
                }
            )
        }
    }
}

@Composable
fun StudentDashboardContent(
    name: String,
    currentUnitName: String?,
    progress: PlacementSummary,
    welcomeTestPending: Boolean,
    welcomeExercises: List<ExerciseDto>,
    completedExerciseIds: Set<Int>,
    isLoading: Boolean,
    onFinishWelcome: (Set<Int>) -> Unit,
) {
    var expandedWelcome by remember { mutableStateOf(welcomeTestPending) }
    var selectedResolved by remember(welcomeExercises, completedExerciseIds) {
        mutableStateOf(welcomeExercises.map { it.id }.filter { it in completedExerciseIds }.toSet())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hola, $name",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131313)
                    )
                    Text(
                        text = if (welcomeTestPending) {
                            "Antes de continuar, realiza el Welcome Test para ubicarte en la unidad correcta."
                        } else {
                            "Tu unidad actual: ${currentUnitName ?: "Sin asignar"}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B6B6B)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (welcomeTestPending) "WELCOME TEST" else "CONTINUAR",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (welcomeTestPending) {
                                    "Test de nivelacion pendiente"
                                } else {
                                    currentUnitName ?: "Unidad asignada"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (progress.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(0.8f).height(6.dp),
                                color = Color(0xFFB8F4C4),
                                trackColor = Color.White.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round,
                            )
                        }
                        IconButton(
                            onClick = { expandedWelcome = !expandedWelcome },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White, RoundedCornerShape(50)),
                        ) {
                            Icon(
                                imageVector = if (welcomeTestPending) Icons.Default.Flag else Icons.Default.PlayArrow,
                                contentDescription = "Accion principal",
                                tint = Color(0xFF003AB6),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Tu progreso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF131313)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Completado",
                    value = "${progress.progressPercent}%",
                    color = Color(0xFFE0F2F1),
                    textColor = Color(0xFF00695C),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Unidades dominadas",
                    value = progress.dominatedUnits.toString(),
                    color = Color(0xFFFFF3E0),
                    textColor = Color(0xFFEF6C00),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            if (welcomeTestPending) {
                WelcomePlacementCard(
                    expanded = expandedWelcome,
                    isLoading = isLoading,
                    welcomeExercises = welcomeExercises,
                    selectedResolved = selectedResolved,
                    onToggleExpand = { expandedWelcome = !expandedWelcome },
                    onToggleExercise = { exerciseId ->
                        selectedResolved = if (exerciseId in selectedResolved) {
                            selectedResolved - exerciseId
                        } else {
                            selectedResolved + exerciseId
                        }
                    },
                    onFinish = {
                        onFinishWelcome(selectedResolved)
                        expandedWelcome = false
                    }
                )
            } else {
                InfoCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Welcome test completado",
                    description = "Ya tienes una unidad asignada en base a tus resultados."
                )
            }
        }

        if (progress.bonusPointsFromSparseUnits > 0) {
            item {
                InfoCard(
                    icon = Icons.Default.School,
                    title = "Estimacion aplicada",
                    description = "Se aplicaron ${progress.bonusPointsFromSparseUnits} punto(s) extra por unidades con pocos ejercicios."
                )
            }
        }
    }
}

@Composable
private fun WelcomePlacementCard(
    expanded: Boolean,
    isLoading: Boolean,
    welcomeExercises: List<ExerciseDto>,
    selectedResolved: Set<Int>,
    onToggleExpand: () -> Unit,
    onToggleExercise: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Realizar Welcome Test", fontWeight = FontWeight.Bold)
                    Text(
                        "Marca los ejercicios que resolviste correctamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                TextButton(onClick = onToggleExpand) {
                    Text(if (expanded) "Ocultar" else "Mostrar")
                }
            }

            if (expanded) {
                if (welcomeExercises.isEmpty()) {
                    Text("Este welcome test aun no tiene ejercicios.", color = Color.Gray)
                } else {
                    welcomeExercises.sortedBy { it.unitId }.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE7EDF3), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = exercise.id in selectedResolved,
                                onCheckedChange = { onToggleExercise(exercise.id) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Unidad ${exercise.unitId}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onFinish,
                    enabled = !isLoading && selectedResolved.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Finalizar test de nivelacion")
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF003AB6))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class PlacementSummary(
    val currentUnitId: Int?,
    val dominatedUnits: Int,
    val completedExercises: Int,
    val totalExercises: Int,
    val progressPercent: Int,
    val bonusPointsFromSparseUnits: Int,
)

private fun estimatePlacement(
    units: List<UnitDto>,
    allExercises: List<ExerciseDto>,
    completedExerciseIds: Set<Int>,
): PlacementSummary {
    if (units.isEmpty()) {
        return PlacementSummary(
            currentUnitId = null,
            dominatedUnits = 0,
            completedExercises = completedExerciseIds.size,
            totalExercises = allExercises.size,
            progressPercent = 0,
            bonusPointsFromSparseUnits = 0
        )
    }

    val exercisesByUnit = allExercises.groupBy { it.unitId }
    val completedByUnit = allExercises
        .filter { it.id in completedExerciseIds }
        .groupingBy { it.unitId }
        .eachCount()

    var dominated = 0
    var highestDominatedUnitId: Int? = null
    var bonusPool = 0
    var bonusFromSparse = 0

    units.sortedBy { it.orderUnit }.forEach { unit ->
        val unitId = unit.id ?: return@forEach
        val totalInUnit = exercisesByUnit[unitId]?.size ?: 0
        val completedInUnit = completedByUnit[unitId] ?: 0

        if (totalInUnit <= 1) {
            if (completedInUnit > 0) {
                bonusPool += completedInUnit
                bonusFromSparse += completedInUnit
                dominated += 1
                highestDominatedUnitId = unitId
            }
            return@forEach
        }

        val required = 2
        val effectiveScore = completedInUnit + bonusPool
        if (effectiveScore >= required) {
            dominated += 1
            highestDominatedUnitId = unitId
            bonusPool = (effectiveScore - required).coerceAtLeast(0)
        }
    }

    val currentUnitId = highestDominatedUnitId ?: units.firstOrNull()?.id

    val totalExercises = allExercises.size
    val completedExercises = allExercises.count { it.id in completedExerciseIds }
    val percent = if (totalExercises > 0) {
        ((completedExercises.toFloat() / totalExercises.toFloat()) * 100f).toInt()
    } else 0

    return PlacementSummary(
        currentUnitId = currentUnitId,
        dominatedUnits = dominated,
        completedExercises = completedExercises,
        totalExercises = totalExercises,
        progressPercent = percent.coerceIn(0, 100),
        bonusPointsFromSparseUnits = bonusFromSparse
    )
}
