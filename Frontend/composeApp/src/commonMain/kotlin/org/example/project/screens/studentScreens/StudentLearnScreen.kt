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
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.example.project.dtos.UnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.screens.studentScreens.StudentExerciseResolverScreen
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.UnitViewModel

private const val UNLOCK_THRESHOLD = 0.8f

class StudentLearnScreen(
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }

        val unitUi by unitVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(userId) {
            unitVm.getAllUnits()
            exercisesVm.getAllExercises()
            if (userId != null && userId > 0) {
                exercisesVm.getExercisesCompletedByUserId(userId)
            }
        }

        LaunchedEffect(unitUi.error, exercisesUi.error) {
            val error = unitUi.error ?: exercisesUi.error
            error?.let { snackbarHostState.showSnackbar(it) }
        }

        val unitProgress = remember(unitUi.units, exercisesUi.exercises, exercisesUi.completedExercises) {
            buildUnitProgress(
                units = unitUi.units,
                allExercises = exercisesUi.exercises,
                completedExerciseIds = exercisesUi.completedExercises.map { it.exerciseId }.toSet()
            )
        }

        StudentAppLayout(
            actualScreen = "Aprender",
            selectedIndex = 1,
            initialUserName = studentName,
            snackbarHostState = snackbarHostState
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
                            UnitCard(
                                progress = progress,
                                onClick = {
                                    val unitId = progress.unit.id ?: return@UnitCard
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
                        }
                    }
                }
            }
        }
    }
}

class StudentUnitExercisesScreen(
    private val unitId: Int,
    private val unitName: String,
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo, unitId = unitId) }
        val exercisesUi by exercisesVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(userId) {
            exercisesVm.getExercisesByUnitId(unitId)
            if (userId != null && userId > 0) {
                exercisesVm.getExercisesCompletedByUserId(userId)
            }
        }

        LaunchedEffect(exercisesUi.error) {
            exercisesUi.error?.let { snackbarHostState.showSnackbar(it) }
        }

        val completedIds = remember(exercisesUi.completedExercises) {
            exercisesUi.completedExercises.map { it.exerciseId }.toSet()
        }
        val availableExercises = remember(exercisesUi.exercises, completedIds) {
            exercisesUi.exercises
                .filter { it.isActive }
                .sortedBy { it.orderExercise }
        }
        val completionRate = remember(availableExercises, completedIds) {
            if (availableExercises.isEmpty()) 0f else availableExercises.count { it.id in completedIds }.toFloat() / availableExercises.size.toFloat()
        }

        StudentAppLayout(
            actualScreen = unitName,
            selectedIndex = 1,
            initialUserName = studentName,
            snackbarHostState = snackbarHostState
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
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { completionRate.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (completionRate >= UNLOCK_THRESHOLD) Color(0xFF2E7D32) else Color(0xFF1565C0)
                                )
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
                            ExerciseCard(
                                exercise = exercise,
                                completed = exercise.id in completedIds,
                                onOpen = {
                                    navigator.push(
                                        StudentExerciseResolverScreen(
                                            exerciseId = exercise.id,
                                            unitId = unitId,
                                            unitName = unitName,
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
private fun UnitCard(
    progress: UnitProgress,
    onClick: () -> Unit,
) {
    val completed = progress.completedCount
    val total = progress.totalCount
    val rate = progress.completionRate

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFC))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(progress.unit.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = progress.unit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF003AB6)
                )
            }

            Text(
                text = "$completed/$total ejercicios (${(rate * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            androidx.compose.material3.LinearProgressIndicator(
                progress = { rate.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExerciseCard(
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
private fun EmptyStateCard(
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

private data class UnitProgress(
    val unit: UnitDto,
    val totalCount: Int,
    val completedCount: Int,
    val completionRate: Float,
)

private fun buildUnitProgress(
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

