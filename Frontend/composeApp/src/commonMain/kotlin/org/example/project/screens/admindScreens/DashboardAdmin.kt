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
import androidx.compose.material.icons.filled.List
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.core.model.rememberScreenModel
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_bold
import org.example.project.components.AppLayout
import org.example.project.dtos.Role
import org.example.project.dtos.UserDto
import org.example.project.network.RepositoryProvider
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
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

class AdminDashboard(private val adminName: String, private val rolAdmin:Role) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        // Obtener ViewModels para mostrar datos reales
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val userVm = rememberScreenModel { UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo) }

        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        // estado para navegación inferior
        var selectedIndex by remember { mutableStateOf(0) } // 0: dashboard, 1: users, 2: levels

        // Usar AppLayout que provee la card principal (bienvenida) y la bottom bar fija
        AppLayout(
            actualScreen = null,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = adminName,
            role = rolAdmin
        ) { paddingValues,_,_ ->

            if (rolAdmin != Role.ADMIN) {
                Column(modifier = Modifier
                    .fillMaxSize()
                ) {
                    Card {
                        Text("No tienes permisos para ver este contenido.")
                    }
                }
                return@AppLayout
            }

            val lessonUnits = unitUi.units.map { u ->
                LessonUnit(
                    id = u.id ?: 0,
                    title = u.name,
                    description = u.description ?: "",
                    status = if (u.isActive) UnitStatus.PUBLISHED else UnitStatus.DRAFT,
                    emoji = "📚"
                )
            }

            // Llamamos al contenido del dashboard, pasando padding desde el layout
            AdminDashboardContent(
                modifier = Modifier
                    .padding(16.dp),
                adminName = adminName,
                lessonUnits = lessonUnits,
                onViewUnit = { id -> navigator.push(UnitDetailsPlaceholder(id)) },
                totalUnits = unitUi.units.size,
                totalUsers = userUi.users.size,
                recentUsers = userUi.users.take(5) // mostrar algunos usuarios recientes
            )
        }
    }
}

@Composable
fun AdminDashboardContent(
    modifier: Modifier = Modifier,
    adminName: String,
    lessonUnits: List<LessonUnit>,
    onViewUnit: (Int) -> Unit, // cambiado: recibir callback por id
    totalUnits: Int,
    totalUsers: Int,
    recentUsers: List<UserDto>
) {
    val weeklyData = listOf(
        WeeklyStats("Lunes", 420, 340),
        WeeklyStats("Martes", 380, 420),
        WeeklyStats("Miércoles", 450, 380),
        WeeklyStats("Jueves", 390, 450),
        WeeklyStats("Viernes", 410, 520),
        WeeklyStats("Sábado", 360, 290),
        WeeklyStats("Domingo", 340, 250)
    )

    var selectedTab by remember { mutableStateOf(1) } // 0: semana, 1: mes, 2: año
    var selectedSection by remember { mutableStateOf(0) } // 0: Análisis, 1: Unidades, 2: Usuarios
    var searchQuery by remember { mutableStateOf("") }

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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Frecuencia de usuarios activos en la app",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2D2D2D),
                    fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Aquí podrías dibujar un gráfico real usando datos reales
                // por ahora dejamos un placeholder visual
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gráfico (datos reales)", color = Color.Gray)
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

                var selectedSectionTabs by remember { mutableStateOf(0) } // 0: Análisis, 1: Unidades, 2: Usuarios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    TextButton(onClick = { selectedSectionTabs = 0 }) {
                        Text(
                            "Análisis",
                            color = if (selectedSectionTabs == 0) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 0) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold))
                        )
                    }
                    TextButton(onClick = { selectedSectionTabs = 1 }) {
                        Text(
                            "Unidades",
                            color = if (selectedSectionTabs == 1) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 1) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold))
                        )
                    }
                    TextButton(onClick = { selectedSectionTabs = 2 }) {
                        Text(
                            "Usuarios",
                            color = if (selectedSectionTabs == 2) Color(0xFFFF6B6B) else Color.Gray,
                            fontWeight = if (selectedSectionTabs == 2) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily(Font(Res.font.encode_sans_bold, weight = FontWeight.Bold))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedSectionTabs) {
                    0 -> AnalysisSection()
                    1 -> UnitsPreviewSection(lessonUnits = lessonUnits, onViewUnit = onViewUnit) // vista compacta (máx 10)
                    2 -> UsersSection(recentUsers)
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
fun UnitsPreviewSection(lessonUnits: List<LessonUnit>, onViewUnit: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Unidades recientes", fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2D2D))
        Spacer(modifier = Modifier.height(8.dp))
        val preview = lessonUnits.take(10)
        if (preview.isEmpty()) {
            Text("No hay unidades disponibles.", color = Color.Gray)
        } else {
            preview.forEach { unit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(unit.title, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(unit.description, color = Color.Gray, maxLines = 2)
                        }
                        IconButton(onClick = { onViewUnit(unit.id) }) {
                            Icon(Icons.Default.List, contentDescription = "Ver detalle")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersSection(users: List<UserDto>) {
    Column {
        Text("Usuarios registrados: ${users.size}", color = Color(0xFF2D2D2D), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        if (users.isEmpty()) {
            Text("No hay usuarios disponibles.", color = Color.Gray)
        } else {
            users.forEach { u ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(u.name, fontWeight = FontWeight.SemiBold)
                            Text(u.email ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                        u.role?.let { Text(it.name, color = Color.Gray) }
                    }
                }
            }
        }
    }
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
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)
        val maxValue = 600f
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
            val x = index * spacing
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