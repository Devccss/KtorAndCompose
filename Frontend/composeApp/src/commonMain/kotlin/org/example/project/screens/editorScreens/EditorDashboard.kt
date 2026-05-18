package org.example.project.screens.editorScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        EditorLayout(
            actualScreen = null,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
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

            }
        }
    }
}


