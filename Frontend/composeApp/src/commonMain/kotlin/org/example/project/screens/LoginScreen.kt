package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.viewModel.UserViewModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import org.example.project.dtos.LoginDto
import org.example.project.dtos.Role
import org.example.project.network.RepositoryProvider
import org.example.project.network.SessionLogTracker
import org.example.project.network.UserSession
import org.example.project.screens.admindScreens.AdminDashboard
import org.example.project.screens.admindScreens.RegisterScreen
import org.example.project.screens.studentScreens.StudentWelcomeScreen
import org.example.project.service.NotificationService

class LoginScreen(private val logout: Boolean? = false, private val userId: Int? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Instancia del ViewModel
        val userViewModel = rememberScreenModel {
            UserViewModel(
                RepositoryProvider.userRepo,
                RepositoryProvider.unitRepo
            )
        }
        val uiState by userViewModel.state.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }
        var autoLoginRequested by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }

        // Gradientes
        val backgroundGradient = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF5F5FA), Color(0xFFF3E8FF))
        )
        val headerGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
        )
        val buttonGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
        )
        val generalMessage = userViewModel.generalMessage

        if (generalMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = generalMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }
        // Mostrar error si existe
        LaunchedEffect(uiState.error) {
            uiState.error?.let {
                snackbarHostState.showSnackbar(it)
                println("Error en UsersScreen: $it") // Log para debugging
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(brush = backgroundGradient)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = headerGradient)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "AP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(40.dp))
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .widthIn(max = 400.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Título y subtítulo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Iniciar Sesión",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF131313)
                                )
                                Text(
                                    text = "Continúa tu aprendizaje",
                                    color = Color(0xFF9B9B9B),
                                    fontSize = 16.sp
                                )
                            }

                            // Error
                            if (showError && uiState.error != null) {
                                println("--->>>>>>>>>>${uiState.error}")
                            }

                            // Campos
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Email
                                Column {
                                    Text(
                                        text = "Correo electrónico",
                                        color = Color(0xFF131313),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = { Text("tu@email.com") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF003AB6),
                                            unfocusedBorderColor = Color(0xFFE5E7EB)
                                        )
                                    )
                                }
                                // Password
                                Column {
                                    Text(
                                        text = "Contraseña",
                                        color = Color(0xFF131313),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Box {
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            placeholder = { Text("••••••••") },
                                            singleLine = true,
                                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF003AB6),
                                                unfocusedBorderColor = Color(0xFFE5E7EB)
                                            ),
                                            trailingIcon = {
                                                IconButton(
                                                    onClick = { showPassword = !showPassword },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                                        tint = Color(0xFF9B9B9B)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Botón de iniciar sesión
                            Button(
                                onClick = {
                                    val loginUser = LoginDto(
                                        email = email,
                                        password = password
                                    )
                                    userViewModel.login(loginUser)

                                    showError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(),
                                enabled = !uiState.isLoading
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(buttonGradient, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            "Iniciar Sesión",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }


                            LaunchedEffect(userId) {
                                if (userId != null && userId > 0 && !autoLoginRequested) {
                                    autoLoginRequested = true
                                    userViewModel.getUserById(userId)
                                }
                            }

                            LaunchedEffect(uiState.currentUser) {
                                if (logout == true) return@LaunchedEffect
                                val currentUser = uiState.currentUser
                                val currentUserId = currentUser?.id

                                if (currentUser?.role == Role.STUDENT && currentUserId != null) {
                                    SessionLogTracker.onUserLoggedIn(currentUserId)
                                    UserSession.set(
                                        currentUserId,
                                        currentUser.name,
                                        currentUser.role,
                                        actualUnit = currentUser.currentUnitId
                                    )
                                    NotificationService.startNotificationPolling()
                                    navigator.push(StudentWelcomeScreen())
                                } else if (currentUser?.role == Role.ADMIN && currentUserId != null) {
                                    SessionLogTracker.onUserLoggedIn(currentUserId)
                                    UserSession.set(
                                        currentUserId,
                                        currentUser.name,
                                        currentUser.role,
                                        actualUnit = currentUser.currentUnitId
                                    )
                                    NotificationService.startNotificationPolling()
                                    navigator.push(AdminDashboard(currentUserId))
                                } else if (currentUser?.role == Role.CONTENT_EDITOR && currentUserId != null) {
                                    SessionLogTracker.onUserLoggedIn(currentUserId)
                                    UserSession.set(
                                        currentUserId,
                                        currentUser.name,
                                        currentUser.role,
                                        actualUnit = currentUser.currentUnitId
                                    )
                                    NotificationService.startNotificationPolling()

                                }
                            }

                            LaunchedEffect(logout) {
                                if (logout == true) {
                                    SessionLogTracker.closeForLogout(UserSession.idUser)
                                    userViewModel.logout()
                                    UserSession.clear()
                                    email = ""
                                    password = ""
                                    showPassword = false
                                    showError = false
                                    navigator.replaceAll(LoginScreen(logout = false, userId = null))
                                }
                            }

                            // ¿No tienes cuenta? Regístrate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "¿No tienes cuenta? ",
                                    color = Color(0xFF9B9B9B),
                                    fontSize = 15.sp
                                )
                                TextButton(
                                    onClick = { navigator.push(RegisterScreen()) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Regístrate",
                                        color = Color(0xFF003AB6),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}