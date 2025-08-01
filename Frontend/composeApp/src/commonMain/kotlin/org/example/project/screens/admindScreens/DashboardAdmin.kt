package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.repository.UsersRepository.UserRepo
import org.example.project.screens.LoginScreen
import org.example.project.viewModel.UserViewModel

data class WeeklyStats(val day: String, val users: Int, val lessons: Int)
data class PopularContent(val title: String, val completions: Int, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
class AdminDashboard(private val adminName: String) : Screen {
    @Composable
    override fun Content() {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navigator.push(route)
                })
            }
        ) {
            Scaffold(
                topBar = {
                    AdminTopBar(
                        currentPage = "dashboard",
                        titlePage = "Admind Dashboard",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            ) { paddingValues ->
                AdminDashboardContent(
                    modifier = Modifier.padding(paddingValues),
                    adminName = adminName,
                    onNavigate = { navigator.push(it) }
                )
            }
        }
    }
}

@Composable
fun AdminDashboardContent(
    modifier: Modifier = Modifier,
    adminName: String,
    onNavigate: (Screen) -> Unit
) {
    val stats = mapOf(
        "totalUsers" to 1247,
        "activeUsers" to 892,
        "totalPhrases" to 2156,
        "completedLessons" to 15432,
        "avgSessionTime" to "12:34",
        "userGrowth" to "+23%"
    )

    val weeklyData = listOf(
        WeeklyStats("Lun", 120, 340),
        WeeklyStats("Mar", 145, 420),
        WeeklyStats("Mié", 167, 380),
        WeeklyStats("Jue", 189, 450),
        WeeklyStats("Vie", 201, 520),
        WeeklyStats("Sáb", 156, 290),
        WeeklyStats("Dom", 134, 250)
    )

    val popularContent = listOf(
        PopularContent("Business Meetings", 1234, "Diálogos"),
        PopularContent("Negotiation Phrases", 987, "Frases"),
        PopularContent("Email Writing", 856, "Nivel 3"),
        PopularContent("Presentation Skills", 743, "Nivel 4"),
        PopularContent("Financial Terms", 692, "Vocabulario")
    )

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "¡Bienvenido, $adminName!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Panel de control de la plataforma AP",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Usuarios Totales", stats["totalUsers"].toString(), stats["userGrowth"].toString())
            AdminStatCard("Usuarios Activos", stats["activeUsers"].toString(), "Últimos 7 días")
            AdminStatCard("Frases Totales", stats["totalPhrases"].toString(), "En la plataforma")
            AdminStatCard("Lecciones Completadas", stats["completedLessons"].toString(), "Total histórico")
            AdminStatCard("Tiempo Promedio", stats["avgSessionTime"].toString(), "Por sesión")
            AdminStatCard("Crecimiento", "+23%", "Usuarios nuevos")
        }

        Spacer(Modifier.height(16.dp))

        Text("Actividad Semanal", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weeklyData) { day -> WeeklyStatsRow(day) }
        }

        Spacer(Modifier.height(16.dp))

        Text("Contenido Más Popular", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(popularContent) { item -> PopularContentRow(item) }
        }
    }
}

@Composable
fun DrawerContent(onNavigate: (Screen) -> Unit) {
    ModalDrawerSheet {
        Text("Menú de Administración", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        NavigationDrawerItem(label = { Text("Dashboard") }, selected = false, onClick = { onNavigate(
            AdminDashboard("Admin")
        ) })
        NavigationDrawerItem(label = { Text("Usuarios") }, selected = false, onClick = { onNavigate(
            LoginScreen()
        ) })
        NavigationDrawerItem(label = { Text("Niveles") }, selected = false, onClick = { onNavigate(
            AdminLevelsScreen()
        ) })
        NavigationDrawerItem(label = { Text("Diálogos") }, selected = false, onClick = { onNavigate(
            DialogsScreen(null)
        ) })
        NavigationDrawerItem(label = { Text("Frases") }, selected = false, onClick = { onNavigate(
            LoginScreen()
        ) })
        NavigationDrawerItem(label = { Text("Palabras") }, selected = false, onClick = { onNavigate(
            LoginScreen()
        ) })
        NavigationDrawerItem(label = { Text("Cerrar Seccion") }, selected = false, onClick = {

            onNavigate(LoginScreen(true))
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(currentPage: String, onBack: () -> Unit, onMenuClick: () -> Unit, titlePage: String) {
    TopAppBar(
        title = { Text(titlePage) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
fun AdminStatCard(title: String, value: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(note, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun WeeklyStatsRow(stat: WeeklyStats) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.day, Modifier.width(40.dp), color = Color.Gray)
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Usuarios: ${stat.users}", color = Color.Black)
                    Text("Lecciones: ${stat.lessons}", color = Color.Gray)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = stat.users / 250f,
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color(0xFF003AB6)
                    )
                    LinearProgressIndicator(
                        progress = stat.lessons / 600f,
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun PopularContentRow(item: PopularContent) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(item.title, style = MaterialTheme.typography.bodyMedium)
            Text(item.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.completions}", color = Color(0xFF003AB6))
            Text("completadas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}