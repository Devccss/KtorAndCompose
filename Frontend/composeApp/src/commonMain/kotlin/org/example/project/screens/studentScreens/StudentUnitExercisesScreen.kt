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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
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
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.FilterTestsDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.TestCompletedDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UnitReviewStatusDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import kotlin.collections.count
import kotlin.collections.orEmpty
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


private const val UNLOCK_THRESHOLD = 0.8f
class StudentUnitExercisesScreen(
    private val unitId: Int,
    private val unitName: String,
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Create ExercisesViewModel without a fixed unitId so we can search by different units
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo, autoLoad = false) }
        val testVm = rememberScreenModel { TestViewModel(RepositoryProvider.testRepo, RepositoryProvider.welcomeTestRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }
        val exercisesUi by exercisesVm.state.collectAsState()
        val unitUi by unitVm.state.collectAsState()
        val testUi by testVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        var selectedIndex by remember { mutableStateOf(1) }
        var assignmentExercises by remember { mutableStateOf<List<ExerciseDto>?>(null) }

        // Recargar búsquedas cuando cambie el usuario o la unidad seleccionada
        LaunchedEffect(userId, unitId) {
            // Buscar ejercicios para la unidad actual. Si hay más de 5 ejercicios en la unidad,
            // solicitar la asignación persistida al backend (/units/{unitId}/assignments/current).
            val safeUserId = userId ?: UserSession.idUser
            try {
                val candidates = RepositoryProvider.exerciseRepo.getExercisesByUnitId(unitId)
                // Si hay más candidatos que el límite, usar la asignación del backend
                if (candidates.size > 5 && safeUserId != null && safeUserId > 0) {
                    try {
                        // Use POST endpoint which returns the existing assignment or creates a new one
                        val assignment = RepositoryProvider.unitRepo.postAssignment(unitId = unitId, mode = "initial", limit = 5)
                        assignmentExercises = assignment.exercises
                    } catch (e: Exception) {
                        println("Error al obtener assignment: ${'$'}{e.message}")
                        // fallback: cargar ejercicios normalmente
                        assignmentExercises = null
                        exercisesVm.searchExercises(FilterExercisesDto(unitId = unitId, isActive = true))
                    }
                } else {
                    // No hace falta usar assignments, cargar ejercicios normalmente
                    assignmentExercises = null
                    exercisesVm.searchExercises(FilterExercisesDto(unitId = unitId, isActive = true))
                }
            } catch (e: Exception) {
                // Si falla la petición de candidates, intentar la carga normal (muestra error si corresponde)
                println("Error cargando ejercicios por unidad: ${'$'}{e.message}")
                assignmentExercises = null
                exercisesVm.searchExercises(FilterExercisesDto(unitId = unitId, isActive = true))
            }
            // Cargar unidades (no depende de unitId, pero es barato)
            unitVm.searchUnits(FilterUnitsDto(isActive = true))
            // Buscar test(s) para la unidad actual y sus ejercicios en tests
            testVm.searchTests(FilterTestsDto(
                unitId = unitId,
                isActive = true,
                name = null
            ))
            testVm.getAllExercisesInTests(onlyActiveTests = true, onlyActiveExercises = true)

            // Si hay un usuario, recargar datos relacionados (ejercicios completados, unidades completadas y usuario)
            if (userId != null && userId > 0) {
                exercisesVm.getExercisesCompletedByUserId(userId)
                unitVm.getAllUnitsCompletedByUserId(userId)
                userVm.getUserById(userId)
            }
        }

        LaunchedEffect(exercisesUi.error, testUi.error, unitUi.error, userUi.error) {
            val error = exercisesUi.error ?: testUi.error ?: unitUi.error ?: userUi.error
            error?.let {
                println("Error en StudentUnitExercisesScreen: $it")
                snackbarHostState.showSnackbar(it)
            }
        }

        val completedIds = remember(exercisesUi.completedExercises) {
            exercisesUi.completedExercises.map { it.exerciseId }.toSet()
        }
        val availableExercises = remember(exercisesUi.exercises, assignmentExercises, completedIds, testUi.allExercisesInTests) {
            val base = assignmentExercises ?: exercisesUi.exercises
            base
                .filter { it.isActive }
                .filter { it.id !in testUi.allExercisesInTests }  // Filtrar ejercicios que no están en tests
                .sortedBy { it.orderExercise }
        }
        val completionRate = remember(availableExercises, completedIds) {
            if (availableExercises.isEmpty()) 0f else availableExercises.count { it.id in completedIds }.toFloat() / availableExercises.size.toFloat()
        }
        val solvedExercises = remember(availableExercises, completedIds) {
            availableExercises.count { it.id in completedIds }
        }
        val isAlreadyMarkedCompleted = remember(unitUi.unitsCompleted, unitId) {
            unitUi.unitsCompleted.any { it.id == unitId }
        }

        LaunchedEffect(userId, unitId, availableExercises, solvedExercises, isAlreadyMarkedCompleted) {
            val safeUserId = userId ?: return@LaunchedEffect
            unitVm.ensureUnitCompletedIfFullySolved(
                userId = safeUserId,
                unitId = unitId,
                totalExercises = availableExercises.size,
                solvedExercises = solvedExercises,
                completedAt = Clock.System.now().toString()
            )
        }

        val unitCompleted = completionRate >= UNLOCK_THRESHOLD
        val unitTest = remember(testUi.searchTest.firstOrNull()) { testUi.searchTest.firstOrNull()}
        val latestAttempt = unitTest?.let { testVm.getLastAttemptForTest(it.id) }
        var reviewStatus by remember { mutableStateOf<UnitReviewStatusDto?>(null) }

        LaunchedEffect(userId, unitTest, unitId) {
            val safeUserId = userId ?: return@LaunchedEffect
            val tId = unitTest?.id ?: run { reviewStatus = null; return@LaunchedEffect }
            try {
                reviewStatus = RepositoryProvider.testRepo.getUnitReviewStatus(safeUserId, unitId, tId)
            } catch (e: Exception) {
                println("Error cargando reviewStatus en UnitExercisesScreen: ${e.message}")
                reviewStatus = null
            }
        }

        val reviewBlocked = unitTest != null && (reviewStatus?.requiresReview ?: false)
        val remainingReviewExercises = reviewStatus?.remainingExercises ?: 0
        val testLocked = unitTest != null && (!isAlreadyMarkedCompleted || (reviewBlocked && remainingReviewExercises > 0))

        LaunchedEffect(isAlreadyMarkedCompleted, unitTest, unitUi.units, userUi.currentUser?.currentUnitId, userId) {
            val safeUserId = userId ?: return@LaunchedEffect
            if (!isAlreadyMarkedCompleted || unitTest != null) return@LaunchedEffect

            val orderedUnits = unitUi.units.sortedBy { it.orderUnit }
            val completedUnitIndex = orderedUnits.indexOfFirst { it.id == unitId }
            if (completedUnitIndex < 0 || completedUnitIndex + 1 >= orderedUnits.size) return@LaunchedEffect

            val nextUnitId = orderedUnits[completedUnitIndex + 1].id ?: return@LaunchedEffect
            val currentUnitId = userUi.currentUser?.currentUnitId ?: UserSession.actualUnit
            if (currentUnitId == nextUnitId) return@LaunchedEffect

            userVm.updateUserCurrentUnit(safeUserId, nextUnitId)
            UserSession.set(
                id = safeUserId,
                actualUnit = nextUnitId
            )
        }

        StudentAppLayout(
            actualScreen = unitName,
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
                            text = unitName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ejercicios disponibles en esta unidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FF))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Progreso de la unidad: ${(completionRate * 100).toInt()}%",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { completionRate.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (completionRate >= UNLOCK_THRESHOLD) Color(0xFF2E7D32) else Color(0xFF1565C0)
                                )
                                if (unitCompleted ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (unitTest != null) {
                                            "✓ Has completado todos los ejercicios. Realiza el test para desbloquear la siguiente unidad."
                                        } else {
                                            "✓ Has completado todos los ejercicios. Esta unidad no tiene test, por lo que se desbloquea la siguiente automáticamente."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (unitTest != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !testLocked) {
                                        if (testLocked) return@clickable
                                        navigator.push(
                                            StudentTestResolverScreen(
                                                testId = unitTest.id,
                                                testName = unitTest.name,
                                                unitId = unitId,
                                                unitName = unitName,
                                                userIdArg = userId,
                                                studentNameArg = studentName
                                            )
                                        )
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        testLocked -> Color(0xFFFFF4E5)
                                        latestAttempt?.score == 100 -> Color(0xFFEAF7EE)
                                        else -> Color(0xFFFFF8E1)
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (testLocked) 0.dp else 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (testLocked) Icons.Default.Lock else Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = if (testLocked) Color.Gray else Color(0xFFF57F17)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Test de unidad", fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = unitTest.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = if (testLocked) Color.Gray else Color(0xFF003AB6)
                                        )
                                    }
                                    Text(
                                        text = when {
                                            !isAlreadyMarkedCompleted -> "Completa la unidad para desbloquear este test."
                                            reviewBlocked && remainingReviewExercises > 0 -> "Repaso pendiente: aprueba $remainingReviewExercises ejercicio(s) para habilitar el reintento del test."
                                            latestAttempt?.score == 100 -> "Test aprobado. Puedes volver a abrirlo si deseas repasar."
                                            else -> "⚠️ Respóndelo con calma: el intento quedará registrado incluso si no apruebas."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (testLocked) Color.Gray.copy(alpha = 0.8f) else Color(0xFF8A5A00)
                                    )
                                }
                            }
                        }
                    }

                    if (availableExercises.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No hay ejercicios activos",
                                description = "Esta unidad todavía no tiene ejercicios listos para resolver."
                            )
                        }
                    } else {
                        items(availableExercises, key = { it.id }) { exercise ->
                            val isCompleted = exercise.id in completedIds
                            ExerciseCard(
                                exercise = exercise,
                                completed = isCompleted,
                                onOpen = {
                                    navigator.push(
                                        StudentExerciseResolverScreen(
                                            exerciseId = exercise.id,
                                            unitId = unitId,
                                            unitName = unitName,
                                            allowCompletedReevaluation = reviewBlocked,
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

@Composable
fun UnitCard(
    progress: UnitProgress,
    isLocked: Boolean = false,
    onClick: () -> Unit,
) {
    val completed = progress.completedCount
    val total = progress.totalCount
    val rate = progress.completionRate

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFE0E0E0) else Color(0xFFF9FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        progress.unit.name,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) Color.Gray else Color.Black
                    )
                    Text(
                        text = progress.unit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLocked) Color.Gray.copy(alpha = 0.6f) else Color.Gray,
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isLocked) Color.Gray else Color(0xFF003AB6)
                )
            }

            Text(
                text = "$completed/$total ejercicios (${(rate * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = if (isLocked) Color.Gray.copy(alpha = 0.6f) else Color.Gray
            )
            LinearProgressIndicator(
                progress = { rate.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (isLocked) Color.Gray else Color(0xFF1565C0),
                trackColor = if (isLocked) Color.Gray.copy(alpha = 0.3f) else Color(0xFFE0E0E0)
            )

            if (isLocked) {
                Text(
                    text = "🔒 Desbloqueada después de completar la unidad anterior",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseDto,
    completed: Boolean,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (completed) Icons.Default.TaskAlt else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (completed) Color(0xFF2E7D32) else Color(0xFF1565C0)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontWeight = FontWeight.SemiBold)
                if (!exercise.description.isNullOrBlank()) {
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }
            if (completed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun UnitTestCard(
    test: TestDto,
    isLocked: Boolean,
    latestAttempt: TestCompletedDto?,
    remainingReviewExercises: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> Color(0xFFFFF4E5)
                latestAttempt?.score == 100 -> Color(0xFFEAF7EE)
                else -> Color(0xFFEFF5FF)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.name,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) Color.Gray else Color.Black
                    )
                    Text(
                        text = test.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLocked) Color.Gray.copy(alpha = 0.6f) else Color.Gray,
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Quiz,
                    contentDescription = null,
                    tint = if (isLocked) Color.Gray else Color(0xFF003AB6)
                )
            }

            when {
                latestAttempt?.score == 100 -> Text(
                    text = "✅ Aprobado. Puedes volver a abrirlo si deseas repasar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
                isLocked -> Text(
                    text = if (remainingReviewExercises > 0) {
                        "🔒 Repaso pendiente: aprueba $remainingReviewExercises ejercicio(s) para reintentar."
                    } else {
                        "🔒 Completa la unidad para desbloquear este test."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.8f)
                )
            }

            if (latestAttempt?.score != null && latestAttempt.score != 100) {
                Text(
                    text = "Último intento: ${latestAttempt.score}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

data class UnitProgress(
    val unit: UnitDto,
    val totalCount: Int,
    val completedCount: Int,
    val completionRate: Float,
)

fun buildUnitProgress(
    units: List<UnitDto>,
    allExercises: List<ExerciseDto>,
    completedExerciseIds: Set<Int>,
): List<UnitProgress> {
    if (units.isEmpty()) return emptyList()

    val exercisesByUnit = allExercises.groupBy { it.unitId }
    return units
        .sortedBy { it.orderUnit }
        .mapNotNull { unit ->
            val unitId = unit.id ?: return@mapNotNull null
            val unitExercises = exercisesByUnit[unitId].orEmpty()
            val total = unitExercises.size
            val completed = unitExercises.count { it.id in completedExerciseIds }
            val rate = if (total == 0) 0f else completed.toFloat() / total.toFloat()
            UnitProgress(
                unit = unit,
                totalCount = total,
                completedCount = completed,
                completionRate = rate
            )
        }
}
