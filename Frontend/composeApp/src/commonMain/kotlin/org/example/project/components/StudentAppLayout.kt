package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.dtos.Role
import org.example.project.network.UserSession
import org.example.project.network.UserSession.role
import org.example.project.screens.LoginScreen
import org.example.project.screens.studentScreens.StudentLearnScreen
import org.example.project.screens.studentScreens.StudentWelcomeScreen
import org.jetbrains.compose.resources.Font

@Composable
fun StudentAppLayout(
    actualScreen: String? = null,
    selectedIndex: Int = 0,
    initialUserName: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (paddingValues: PaddingValues, userName: String, role: Role) -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow

    val sessionName = UserSession.name
    val sessionRole = role
    val userName = remember(initialUserName, sessionName) { initialUserName ?: sessionName ?: "Estudiante" }
    val userRole = remember(role, sessionRole) { role ?: sessionRole ?: Role.STUDENT }
    val userId = UserSession.idUser

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            StudentBottomBar(
                selectedIndex = selectedIndex,
                onNavigate = { index ->
                    when (index) {
                        0 -> navigator.replaceAll(StudentWelcomeScreen(userId))
                        1 -> navigator.replaceAll(StudentLearnScreen(userId, userName))
                        2 -> {
                            UserSession.clear()
                            navigator.replaceAll(LoginScreen())
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F0))
                .padding(paddingValues)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Cambio de lógica: Si hay actualScreen úsalo, si no, usa el mensaje de bienvenida
                        (actualScreen ?: if (userName.isNotBlank()) "¡Bienvenido $userName!" else null)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2D2D2D),
                                fontFamily = FontFamily(Font(Res.font.encode_sans_variable, weight = FontWeight.SemiBold))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userRole.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontFamily = FontFamily(Font(Res.font.jetbrains_mono_regular)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A4A4A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Contenido de la pantalla recibe paddingValues y datos del usuario
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                content(paddingValues, userName, userRole)
            }
        }

    }
}

@Composable
private fun StudentBottomBar(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = selectedIndex == 0,
            onClick = { onNavigate(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.School, contentDescription = "Aprender") },
            label = { Text("Aprender") },
            selected = selectedIndex == 1,
            onClick = { onNavigate(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Salir") },
            label = { Text("Salir") },
            selected = false,
            onClick = { onNavigate(2) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.error
            )
        )
    }
}
