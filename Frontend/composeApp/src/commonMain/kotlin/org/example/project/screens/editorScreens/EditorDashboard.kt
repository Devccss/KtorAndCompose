package org.example.project.screens.editorScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_bold
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.EditorLayout
import org.example.project.dtos.StudentsStatsSummaryDto
import org.example.project.dtos.WeeklySessionMetricDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.screens.admindScreens.LineChart
import org.example.project.screens.admindScreens.StatRow
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.SessionLogViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import org.jetbrains.compose.resources.Font



class EditorDashboard(private val id: Int? = null ) : Screen {
    @Composable
    override fun Content() {
        // Obtener ViewModels para mostrar datos reales
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }
        val exerciseVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val sessionLogVm = rememberScreenModel { SessionLogViewModel(RepositoryProvider.sessionLogRepo,true) }
        val snackbarHostState = remember { SnackbarHostState() }

        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()
        val exerciseUi by exerciseVm.state.collectAsState()
        val sessionUi by sessionLogVm.state.collectAsState()

        // estado para navegación inferior
        var selectedIndex by remember { mutableStateOf(0) }

        var totalUnits by remember { mutableStateOf(0) }
        var totalUsers by remember { mutableStateOf(0) }
        var totalExercises by remember { mutableStateOf(0) }

        LaunchedEffect(unitUi.error, userUi.error, exerciseUi.error) {
            if (!unitUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error cargando unidades: ${unitUi.error}")
                println("Error cargando unidades: ${unitUi.error}")
            }
            if (!userUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error cargando usuarios: ${userUi.error}")
                println("Error cargando usuarios: ${userUi.error}")
            }
            if (!exerciseUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error cargando ejercicios: ${exerciseUi.error}")
                println("Error cargando ejercicios: ${exerciseUi.error}")
            }
        }

        LaunchedEffect(unitUi.units, userUi.users, exerciseUi.exercises) {
            totalUnits = unitUi.units.size
            totalUsers = userUi.users.size
            totalExercises = exerciseUi.exercises.size
        }
        LaunchedEffect(Unit) {
            userVm.loadStudentsStats()
        }
        LaunchedEffect(id){
            id?.let { userId ->
                if (userId > 0) {
                    userVm.getUserById(userId)
                }
            }
        }



        // Usar AppLayout que provee la card principal (bienvenida) y la bottom bar fija
        EditorLayout(
            actualScreen = null,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->


            // Llamamos al contenido del dashboard, pasando padding desde el layout
            EditorDashboardContent(
                editirName = UserSession.name ?: "Unknown",
                totalUnits = totalUnits,
                totalUsers = totalUsers,
                totalExercises = totalExercises,
                weeklyMetrics = sessionUi.weeklyMetrics,
                sessionMetricsLoading = sessionUi.isLoading,
                sessionMetricsError = sessionUi.error,
                onRetryLoadMetrics = { sessionLogVm.loadWeeklyMetrics() },
                studentsStats = userUi.studentsStats,
                studentsStatsLoading = userUi.studentsStatsLoading,
                studentsStatsError = userUi.studentsStatsError,
                onRetryLoadStudentsStats = { userVm.loadStudentsStats() }
            )
        }
    }
}

@Composable
fun EditorDashboardContent(
    modifier: Modifier = Modifier,
    editirName: String,
    totalUnits: Int,
    totalUsers: Int,
    totalExercises: Int,
    weeklyMetrics: List<WeeklySessionMetricDto>,
    sessionMetricsLoading: Boolean,
    sessionMetricsError: String?,
    onRetryLoadMetrics: () -> Unit,
    studentsStats: StudentsStatsSummaryDto?,
    studentsStatsLoading: Boolean,
    studentsStatsError: String?,
    onRetryLoadStudentsStats: () -> Unit
) {
    val chartData = remember(weeklyMetrics) {
        weeklyMetrics.map { (it.averageDurationSeconds.toFloat() / 60f).coerceAtLeast(0f) }
    }
    val chartLabels = remember(weeklyMetrics) {
        weeklyMetrics.map {
            val date = it.weekStart.take(10)
            if (date.length >= 10) date.substring(5, 10) else date
        }
    }
    val latestMetric = weeklyMetrics.lastOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Panel del editor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = "Hola, $editirName. Aquí puedes ver el resumen real del alumnado y su rendimiento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Duracion promedio semanal de sesiones (min)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2D2D2D),
                        fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        sessionMetricsLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFF6B6B))
                            }
                        }

                        !sessionMetricsError.isNullOrBlank() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = sessionMetricsError,
                                    color = Color(0xFFB00020),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                TextButton(onClick = onRetryLoadMetrics) {
                                    Text("Reintentar")
                                }
                            }
                        }

                        chartData.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin datos de sesiones", color = Color.Gray)
                            }
                        }

                        else -> {
                            LineChart(
                                data = chartData,
                                labels = chartLabels,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            latestMetric?.let { metric ->
                                StatRow("Usuarios activos (ultima semana)", metric.activeUsers.toString())
                                StatRow("Sesiones (ultima semana)", metric.totalSessions.toString())
                                StatRow("Duracion total (ultima semana)", "${metric.totalDurationSeconds / 60} min")
                            }
                        }
                    }
                }
            }
        }

        item {
            StudentsOverviewCard(
                studentsStats = studentsStats,
                loading = studentsStatsLoading,
                error = studentsStatsError,
                onRetry = onRetryLoadStudentsStats
            )
        }
    }
}

@Composable
private fun StudentsOverviewCard(
    studentsStats: StudentsStatsSummaryDto?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Estadísticas generales de alumnos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF6B6B))
                    }
                }

                !error.isNullOrBlank() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = error,
                            color = Color(0xFFB00020),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onRetry) { Text("Reintentar") }
                    }
                }

                studentsStats == null -> {
                    Text(
                        text = "Sin estadísticas de alumnos por el momento.",
                        color = Color.Gray
                    )
                }

                else -> {
                    SummaryMetricRow(
                        first = "Alumnos",
                        firstValue = studentsStats.totalStudents.toString(),
                        second = "Unidades",
                        secondValue = studentsStats.completedUnitsCount.toString()
                    )
                    SummaryMetricRow(
                        first = "Ejercicios",
                        firstValue = studentsStats.completedExercisesCount.toString(),
                        second = "Tests",
                        secondValue = studentsStats.completedTestsCount.toString()
                    )
                    SummaryMetricRow(
                        first = "Fallos en tests",
                        firstValue = studentsStats.failedTestsCount.toString(),
                        second = "Horas de estudio",
                        secondValue = studentsStats.totalStudyHours.formatHours()
                    )
                    SummaryMetricCard(
                        title = "Promedio general de tests",
                        value = "${studentsStats.averageTestScore.formatScore()}%",
                        accent = Color(0xFFFFE7D1)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryMetricRow(
    first: String,
    firstValue: String,
    second: String,
    secondValue: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryMetricCard(title = first, value = firstValue, accent = Color(0xFFF7FFF3), modifier = Modifier.weight(1f))
        SummaryMetricCard(title = second, value = secondValue, accent = Color(0xFFFFF9EA), modifier = Modifier.weight(1f))
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = accent),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
        }
    }
}

fun Double.formatHours(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

fun Double.formatScore(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
