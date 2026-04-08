package org.example.project.screens.studentScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.example.project.components.StudentAppLayout
import org.example.project.network.UserSession

class StudentWelcomeScreen(val studentName: String) : Screen {
    @Composable
    override fun Content() {
        // Usamos StudentAppLayout en lugar de AppLayout
        StudentAppLayout(
            actualScreen = "Inicio",
            selectedIndex = 0,
            initialUserName = UserSession.name ?: "Estudiante",
            snackbarHostState = remember { SnackbarHostState() }
        ) { _, _, _ ->
            StudentDashboardContent(studentName)
        }
    }
}

@Composable
fun StudentDashboardContent(name: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
        ) {
            // Header de Bienvenida
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¡Hola, ${UserSession.name ?: "Estudiante"}! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131313)
                )
                Text(
                    text = "¿Listo para aprender algo nuevo hoy?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9B9B9B)
                )
            }

            // Tarjeta de "Continuar Aprendiendo"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONTINUAR",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unidad 1: Introducción",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { 0.3f },
                                modifier = Modifier.fillMaxWidth(0.8f).height(6.dp),
                                color = Color(0xFFB8F4C4),
                                trackColor = Color.White.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round,
                            )
                        }
                        IconButton(
                            onClick = { /* Navegar a lección */ },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White, RoundedCornerShape(50)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Continuar",
                                tint = Color(0xFF003AB6),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Sección de Progreso
            Text(
                text = "Tu progreso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF131313)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Completado",
                    value = "15%",
                    color = Color(0xFFE0F2F1), // Tono verdoso suave
                    textColor = Color(0xFF00695C),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Racha",
                    value = "1 días",
                    color = Color(0xFFFFF3E0), // Tono naranja suave
                    textColor = Color(0xFFEF6C00),
                    modifier = Modifier.weight(1f)
                )
            }
            TextButton(
                onClick = { /* Navegar a test de nivelación */ },
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF003AB6))
            ){
                Text("Realizar test de nivelación")
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}