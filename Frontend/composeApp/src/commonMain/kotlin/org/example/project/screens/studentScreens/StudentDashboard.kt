package org.example.project.screens.studentScreens

import RepositoryProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.dtos.UsersDto
import org.example.project.models.Users
import org.example.project.screens.LoginScreen
import org.example.project.viewModel.StudentViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StudentTopBar(
    currentPage: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    titlePage: String
) {
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
        }
    )
}

class StudentDashboard(private val student: UsersDto) : Screen {
    @Composable
    override fun Content() {
        val studentView =
            rememberScreenModel { StudentViewModel(RepositoryProvider.studentRepository,student) }
        val uiState by studentView.state.collectAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow




        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                studentDrawerContent(onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navigator.push(route)
                }, drawStudent = student)
            }
        ) {
            Scaffold(
                topBar = {
                    StudentTopBar(
                        currentPage = "studentDashboard",
                        titlePage = "Student Dashboard",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(it)) {
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
                                "¡Bienvenido, ${uiState.studentDetails?.name ?: "Estudiante"}!",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Panel de control de la plataforma AP",
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Progreso del Estudiante",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text("Nivel Actual: ${uiState.studentDetails?.currentLevelId ?: "Desconocido"}")
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                "Total Test Score: ${
                                    uiState.studentProgress?.testScore
                                        ?: "No disponible"
                                }"
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                "Diálogos Completados: ${
                                    uiState.studentProgress?.completedDialogs
                                        ?: "No disponible"
                                }"
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(uiState.studentProgress){

        }

    }
}

@Composable
fun studentDrawerContent(onNavigate: (Screen) -> Unit, drawStudent: UsersDto) {
    ModalDrawerSheet {
        Text(
            "Menú",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        NavigationDrawerItem(label = { Text("Dashboard") }, selected = false, onClick = {
            onNavigate(
                StudentDashboard(drawStudent)
            )
        })

        NavigationDrawerItem(label = { Text("Niveles") }, selected = false, onClick = {
            onNavigate(
                StudentLevelsScreen()
            )
        })

        NavigationDrawerItem(label = { Text("Cerrar Sesión") }, selected = false, onClick = {

            onNavigate(LoginScreen(true))
        })
    }
}