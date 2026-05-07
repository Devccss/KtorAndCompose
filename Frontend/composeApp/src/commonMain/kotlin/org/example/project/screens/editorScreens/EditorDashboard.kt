package org.example.project.screens.editorScreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.example.project.dtos.Role
import org.example.project.dtos.WeeklySessionMetricDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
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

        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()
        val exerciseUi by exerciseVm.state.collectAsState()
        val sessionUi by sessionLogVm.state.collectAsState()

        // estado para navegación inferior
        var selectedIndex by remember { mutableStateOf(0) }

        var totalUnits by remember { mutableStateOf(0) }
        var totalUsers by remember { mutableStateOf(0) }
        var totalExercises by remember { mutableStateOf(0) }

        LaunchedEffect(unitUi.units, userUi.users, exerciseUi.exercises) {
            totalUnits = unitUi.units.size
            totalUsers = userUi.users.size
            totalExercises = exerciseUi.exercises.size
        }
        LaunchedEffect(id){
            id?.let { userId ->
                if (userId > 0) {
                    userVm.getUserById(userId)
                }
            }
        }



        // Usar AppLayout que provee la card principal (bienvenida) y la bottom bar fija
        AppLayout(
            actualScreen = null,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
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
            EditorDashboardContent(
                editirName = UserSession.name?: "Unknown",
                totalUnits = totalUnits,
                totalUsers = totalUsers,
                totalExercises = totalExercises,
                weeklyMetrics = sessionUi.weeklyMetrics,
                sessionMetricsLoading = sessionUi.isLoading,
                sessionMetricsError = sessionUi.error,
                onRetryLoadMetrics = { sessionLogVm.loadWeeklyMetrics() }
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

        // Sección de estadísticas con tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        "Análisis" ,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold)),
                    )
                }
                var selectedSectionTabs by remember { mutableStateOf(0) } // 0: Análisis, 1: Unidades, 2: Usuarios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    TextButton(onClick = { selectedSectionTabs = 0 }) {
                        Text(
                            "General",
                            color = if (selectedSectionTabs == 0) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 0) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.jetbrains_mono_regular)),
                            fontSize = 14.sp
                        )
                    }
                    TextButton(onClick = { selectedSectionTabs = 2 }) {
                        Text(
                            "Usuarios",
                            color = if (selectedSectionTabs == 2) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 2) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.jetbrains_mono_regular)),
                            fontSize = 14.sp
                        )
                    }
                    TextButton(onClick = { selectedSectionTabs = 1 }) {
                        Text(
                            "Unidades",
                            color = if (selectedSectionTabs == 1) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 1) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.jetbrains_mono_regular)),
                            fontSize = 14.sp
                        )
                    }
                    TextButton(onClick = { selectedSectionTabs = 3 }) {
                        Text(
                            "Ejercicios",
                            color = if (selectedSectionTabs == 3) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 3) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.jetbrains_mono_regular)),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedSectionTabs) {
                    0 -> AnalysisSection()
                    1 -> UnitPreviewSection(totalUnits)
                    2 -> UsersSection(totalUsers)
                    3 -> ExerciseSection(totalExercises)
                }
            }
        }
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
