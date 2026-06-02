package org.example.project.screens.admindScreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_bold
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.GeneralStatsDto
import org.example.project.dtos.Role
import org.example.project.dtos.StudentsStatsSummaryDto
import org.example.project.dtos.WeeklySessionMetricDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.screens.editorScreens.SummaryMetricCard
import org.example.project.screens.editorScreens.SummaryMetricRow
import org.example.project.screens.editorScreens.formatHours
import org.example.project.screens.editorScreens.formatScore
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.SessionLogViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import org.example.project.viewModel.UsersUiState
import org.jetbrains.compose.resources.Font

data class WeeklyStats(val day: String, val users: Int, val lessons: Int)
data class PopularContent(val title: String, val completions: Int, val category: String)
data class LessonUnit(
    val id: Int,
    val title: String,
    val description: String,
    val status: UnitStatus,
    val emoji: String
)

enum class UnitStatus {
    DRAFT, PUBLISHED
}

class AdminDashboard(private val id: Int? = null ) : Screen {
    @Composable
    override fun Content() {
        // Obtener ViewModels para mostrar datos reales
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }
        val exerciseVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val sessionLogVm = rememberScreenModel { SessionLogViewModel(RepositoryProvider.sessionLogRepo,false) }

        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()
        val exerciseUi by exerciseVm.state.collectAsState()
        val sessionUi by sessionLogVm.state.collectAsState()

        // estado para navegación inferior
        var selectedIndex by remember { mutableStateOf(0) }

        var totalUnits by remember { mutableStateOf(0) }
        var totalUsers by remember { mutableStateOf(0) }
        var totalExercises by remember { mutableStateOf(0) }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(unitUi.units, userUi.users, exerciseUi.exercises) {
            totalUnits = unitUi.units.size
            totalUsers = userUi.users.size
            totalExercises = exerciseUi.exercises.size
        }
        LaunchedEffect( unitUi.error, userUi.error, exerciseUi.error, sessionUi.error) {
            if (!unitUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error al cargar unidades: ${unitUi.error}")
                println("Error al cargar unidades: ${unitUi.error}")
            }
            if (!userUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error al cargar usuarios: ${userUi.error}")
                println("Error al cargar usuarios: ${userUi.error}")
            }
            if (!exerciseUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error al cargar ejercicios: ${exerciseUi.error}")
                println("Error al cargar ejercicios: ${exerciseUi.error}")
            }
            if (!sessionUi.error.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Error al cargar métricas de sesiones: ${sessionUi.error}")
                println("Error al cargar métricas de sesiones: ${sessionUi.error}")
            }
        }
        LaunchedEffect(id){
            id?.let { userId ->
                if (userId > 0) {
                    userVm.getUserById(userId)
                }
            }
        }
        LaunchedEffect(Unit) {
            userVm.loadAllUsersStats()
        }



        // Usar AppLayout que provee la card principal (bienvenida) y la bottom bar fija
        AppLayout(
            actualScreen = null,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = remember { SnackbarHostState() },
        ) { _,_,_ ->

            if (UserSession.role != Role.ADMIN) {
                Column(modifier = Modifier
                    .fillMaxSize()
                ) {
                    Card {
                        Text("No tienes permisos para ver este contenido.")
                    }
                }
                return@AppLayout
            }


            // Llamamos al contenido del dashboard, pasando padding desde el layout
            AdminDashboardContent(
                userUi = userUi,
                weeklyMetrics = sessionUi.weeklyMetrics,
                sessionMetricsLoading = sessionUi.isLoading,
                sessionMetricsError = sessionUi.error,
                onRetryLoadMetrics = { sessionLogVm.loadWeeklyMetrics() }
            )
        }
    }
}

@Composable
fun AdminDashboardContent(
    modifier: Modifier = Modifier,
    userUi : UsersUiState,
    weeklyMetrics: List<WeeklySessionMetricDto>,
    sessionMetricsLoading: Boolean,
    sessionMetricsError: String?,
    onRetryLoadMetrics: () -> Unit
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
            .verticalScroll(rememberScrollState())
            // .padding(16.dp) // padding ya aplicado por quien llama
        ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

        OverviewCard(
            generalStats = userUi.allUserStats, // Aquí podrías pasar stats generales si los obtienes
            loading = false,
            error = null,
            onRetry = {}
        )
    }
}

@Composable
fun AnalysisSection() {
    Column {
        StatRow("Alumnos actualmente en línea", "20")
        StatRow("Alumnos activos esta semana", "100")
        StatRow("Alumnos inactivos", "200")
        StatRow("Alumnos que completaron el contenido", "2")
        StatRow("Alumnos en racha 7+ días", "25")
        StatRow("Alumnos en racha 7- días", "300")
    }
}

@Composable
fun UnitPreviewSection(units: Int) {
    Column {
        StatRow("Unidades totales", "$units")

    }
}
@Composable
fun UsersSection(users: Int) {
    StatRow("Alumnos totales", "$users")
}

@Composable
fun ExerciseSection(exercises: Int) {
    StatRow("Alumnos totales", "$exercises")
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isSelected) Color(0xFFFF6B6B) else Color.Gray
        )
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxDataValue = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (data.isEmpty()) return@Canvas

        val spacing = if (data.size > 1) width / (data.size - 1) else 0f
        val maxValue = maxDataValue * 1.15f
        val minValue = 0f

        // Dibujar líneas de la cuadrícula
        val gridLines = 7
        for (i in 0..gridLines) {
            val y = height - (height * i / gridLines)
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Dibujar línea de datos
        val path = Path()
        val points = data.mapIndexed { index, value ->
            val x = if (data.size == 1) width / 2f else index * spacing
            val normalizedValue = (value - minValue) / (maxValue - minValue)
            val y = height - (normalizedValue * height * 0.8f) - (height * 0.1f)
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFFFF6B6B),
            style = Stroke(width = 3.dp.toPx())
        )

        // Dibujar puntos
        points.forEach { point ->
            drawCircle(
                color = Color(0xFFFF6B6B),
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }

    // Etiquetas del eje X
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2D2D2D)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B6B)
        )
    }
    HorizontalDivider(color = Color(0xFFE0E0E0))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(currentPage: String, onBack: () -> Unit, onMenuClick: () -> Unit, titlePage: String) {
    TopAppBar(
        title = { Text(titlePage) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

// Placeholder de detalle de unidad (reemplazar por tu pantalla de detalle real)
class UnitDetailsPlaceholder(private val unitId: Int) : Screen {
    @Composable
    override fun Content() {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Detalle de unidad (placeholder)", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("ID: $unitId")
        }
    }
}

@Composable
private fun OverviewCard(
    generalStats: GeneralStatsDto?,
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
                text = "Estadísticas generales de los usuarios",
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

                generalStats == null -> {
                    Text(
                        text = "Sin estadísticas de usuarios por el momento.",
                        color = Color.Gray
                    )
                }

                else -> {
                    SummaryMetricRow(
                        first = "Alumnos",
                        firstValue = generalStats.totalUsers.toString(),
                        second = "Unidades",
                        secondValue = generalStats.totalUnits.toString()
                    )
                    SummaryMetricRow(
                        first = "Ejercicios",
                        firstValue = generalStats.totalExercises.toString(),
                        second = "Tests",
                        secondValue = generalStats.totalTests.toString()
                    )
                    SummaryMetricRow(
                        first = "Fallos en tests",
                        firstValue = generalStats.totalFailedTests.toString(),
                        second = "Horas de estudio",
                        secondValue = generalStats.totalStudyHours.formatHours()
                    )
                    SummaryMetricCard(
                        title = "Promedio general de tests",
                        value = "${generalStats.averageTestScore.formatScore()}%",
                        accent = Color(0xFFFFE7D1)
                    )
                }
            }
        }
    }
}