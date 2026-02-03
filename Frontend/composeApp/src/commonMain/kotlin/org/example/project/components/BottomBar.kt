package org.example.project.components

// ...existing imports...
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.dtos.Role
import org.example.project.screens.admindScreens.AdminDashboard
import org.example.project.screens.admindScreens.UsersScreen
import org.example.project.screens.admindScreens.UnitsScreen
import org.example.project.screens.LoginScreen
import org.example.project.network.UserSession

data class NavItem(val id: Int, val icon: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableBottomBar(
    modifier: Modifier = Modifier.fillMaxWidth(),
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: ((Int) -> Unit)? = null,
    initialUserName: String? = null,
    role: Role? = null,
) {
    // Inicializar valores desde initial params o desde UserSession si no se pasan.
    // Usamos rememberSaveable para mantener entre recomposiciones; UserSession mantiene entre pantallas.
    val sessionName = UserSession.name
    val sessionRole = UserSession.role

    var rememberedUserName by rememberSaveable { mutableStateOf(initialUserName ?: sessionName ?: "") }
    var rememberedRoleName by rememberSaveable { mutableStateOf(role?.name ?: sessionRole?.name ?: Role.STUDENT.name) }

    // Si se recibe explicitamente initialUserName/role, sincronizamos UserSession
    if (!initialUserName.isNullOrBlank() || role != null) {
        // usamos remember{} para evitar ejecutar set muchas veces en recomposiciones
        remember(initialUserName, role) {
            UserSession.set(initialUserName ?: sessionName, role ?: sessionRole)
        }
    }

    val navigator = LocalNavigator.currentOrThrow

    // Convertir nombre del rol a enum con fallback
    val rememberUserRole: Role = try {
        Role.valueOf(rememberedRoleName)
    } catch (e: Exception) {
        Role.STUDENT
    }

    Surface(
        modifier = modifier
            // asegurarse de respetar la barra de navegación del sistema
            .navigationBarsPadding(),
        tonalElevation = 4.dp,
        color = Color.White
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                // también aplicamos navigationBarsPadding al NavigationBar por seguridad
                .navigationBarsPadding(),
            containerColor = Color.White,
            tonalElevation = 4.dp
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                // animación simple: escala del icono
                val scale by animateFloatAsState(if (selected) 1.15f else 1f)

                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .scale(scale)
                        ) {
                            Icon(item.icon, contentDescription = item.label, tint = if (selected) Color(0xFF003AB6) else Color(0xFF9B9B9B))
                            AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .height(4.dp)
                                        .width(24.dp)
                                        .background(Color(0xFF003AB6), shape = CircleShape)
                                )
                            }
                            // Label pequeño
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) Color(0xFF003AB6) else Color(0xFF9B9B9B),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    selected = selected,
                    onClick = {
                        // Actualizar estado visual si se proporciona callback
                        onSelect?.invoke(index)

                        // Si se pasó initialUserName/role actualizamos UserSession (ya hecho arriba),
                        // sino usamos lo que hay en rememberedUserName/rememberUserRole (llenados desde session si existía).
                        val nameToUse = if (rememberedUserName.isNotBlank()) rememberedUserName else (UserSession.name ?: "")
                        val roleToUse = rememberUserRole

                        // Manejo centralizado de navegación según índice usando los datos persistentes
                        when (index) {
                            0 -> { // Inicio
                                if (navigator != null) {
                                    navigator.push(AdminDashboard(nameToUse.ifBlank { "Usuario" }, roleToUse))
                                }
                            }
                            1 -> { // Usuarios
                                navigator?.push(UsersScreen())
                            }
                            2 -> { // Unidades
                                navigator?.push(UnitsScreen())
                            }
                            3 -> { // Logout / Salir
                                // limpiar sesión al hacer logout
                                UserSession.clear()
                                navigator?.push(LoginScreen(true))
                            }
                            else -> { /* otros índices si los hay */ }
                        }
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF003AB6),
                        unselectedIconColor = Color(0xFF9B9B9B),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}