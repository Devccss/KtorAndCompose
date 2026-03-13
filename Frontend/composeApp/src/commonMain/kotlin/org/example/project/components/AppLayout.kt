package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.navigator.LocalNavigator
import org.example.project.dtos.Role
import org.example.project.network.UserSession
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.screens.admindScreens.UserDetailsScreen
import org.jetbrains.compose.resources.Font


@Composable
fun AppLayout(
    actualScreen: String? = null,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    initialUserName: String? = null,
    role: Role? = null,
    id: Int? = null,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (paddingValues: PaddingValues, userName: String, role: Role) -> Unit
) {
    val sessionId = UserSession.idUser
    val sessionName = UserSession.name
    val sessionRole = UserSession.role
    val userName = remember(initialUserName, sessionName) { initialUserName ?: sessionName ?: "" }
    val userRole = remember(role, sessionRole) { role ?: sessionRole ?: Role.STUDENT }
    val navigator = LocalNavigator.current

    LaunchedEffect(initialUserName, role) {
        if (!initialUserName.isNullOrBlank() || role != null) {
            UserSession.set(sessionId?: id ?: -1 ,initialUserName ?: sessionName, role ?: sessionRole)
        }
    }

    val snackbarHost = rememberUpdatedState(snackbarHostState)

    Scaffold(
        modifier = modifier.navigationBarsPadding(),
        snackbarHost = {
            if (snackbarHost.value != null) {
                SnackbarHost(snackbarHost.value!!)
            } else {
                SnackbarHost(SnackbarHostState())
            }
        },
        bottomBar = {
            // ahora ReusableBottomBar no requiere items
            ReusableBottomBar(
                selectedIndex = selectedIndex,
                onSelect = { idx -> onSelect(idx) },
                initialUserName = userName.ifBlank { null },
                role = userRole
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
                        IconButton(
                            onClick = {
                                if (sessionId != -1 && sessionId != null) {
                                    navigator?.push(UserDetailsScreen(sessionId))

                                }
                            }
                        ){
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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