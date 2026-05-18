package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import org.example.project.dtos.NotificationSubCategory
import org.example.project.dtos.Role
import org.example.project.network.UserSession
import org.example.project.network.UserSession.role
import org.example.project.service.NotificationService
import org.example.project.screens.LoginScreen
import org.example.project.screens.studentScreens.StudentLearnScreen
import org.example.project.screens.studentScreens.StudentMeUserScreen
import org.example.project.screens.studentScreens.StudentWelcomeScreen
import org.example.project.viewModel.NotificationViewModel
import org.jetbrains.compose.resources.Font

@Composable
fun StudentAppLayout(
    actualScreen: String? = null,
    selectedIndex: Int,
    initialUserName: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },

    onSelect: (Int) -> Unit,
    content: @Composable (paddingValues: PaddingValues, userName: String, role: Role) -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    val sessionName = UserSession.name
    val sessionRole = role
    val userName = remember(initialUserName, sessionName) { initialUserName ?: sessionName ?: "Estudiante" }
    val userRole = remember(role, sessionRole) { role ?: sessionRole ?: Role.STUDENT }
    val userId = UserSession.idUser
    val notificationVm = remember { NotificationViewModel(NotificationService()) }
    val notificationState by notificationVm.state.collectAsState()
    var notificationsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let { notificationVm.loadByUser(it, unreadOnly = false) }
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ReusableStudentBottomBar(
                selectedIndex = selectedIndex,
                onSelect = { idx -> onSelect(idx) }
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

                    Box {
                        BadgedBox(
                            badge = {
                                if (notificationState.unreadCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(
                                            text = if (notificationState.unreadCount > 99) "99+" else notificationState.unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { notificationsExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color(0xFF4A4A4A)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = notificationsExpanded,
                            onDismissRequest = { notificationsExpanded = false }
                        ) {
                            if (notificationState.notifications.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No tienes notificaciones") },
                                    onClick = { notificationsExpanded = false }
                                )
                            } else {
                                notificationState.notifications.forEach { notification ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = notification.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = notification.message,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        onClick = {
                                            scope.launch {
                                                notificationVm.markAsRead(notification.id)
                                                notificationsExpanded = false
                                                if (notification.subCategory == NotificationSubCategory.UNIT_COMPLETED) {
                                                    navigator.replaceAll(StudentLearnScreen(userId, userName))
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { UserSession.idUser?.let { navigator.push(StudentMeUserScreen(it)) } },
                        modifier = Modifier.size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4A4A)),
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
