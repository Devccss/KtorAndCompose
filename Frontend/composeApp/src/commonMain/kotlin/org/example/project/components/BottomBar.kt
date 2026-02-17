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
import androidx.compose.material.icons.automirrored.filled.List
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
import org.example.project.network.UserSession
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Check
import org.example.project.screens.admindScreens.ExercisesScreen

data class NavItem(val id: Int, val icon: ImageVector, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableBottomBar(
    modifier: Modifier = Modifier.fillMaxWidth(),
    selectedIndex: Int,
    onSelect: ((Int) -> Unit)? = null,
    initialUserName: String? = null,
    role: Role? = null,
) {
    // Lista fija de items para toda la app
    val fixedItems = listOf(
        NavItem(0, Icons.Default.Home, "Inicio"),
        NavItem(1, Icons.Default.Group, "Usuarios"),
        NavItem(2, Icons.AutoMirrored.Filled.List, "Unidades"),
        NavItem(3, Icons.Default.Check, "Ejercicios")
    )

    // Inicializar valores desde initial params o desde UserSession si no se pasan.
    val sessionName = UserSession.name
    val sessionRole = UserSession.role

    var rememberedUserName by rememberSaveable { mutableStateOf(initialUserName ?: sessionName ?: "") }
    var rememberedRoleName by rememberSaveable { mutableStateOf(role?.name ?: sessionRole?.name ?: Role.STUDENT.name) }

    if (!initialUserName.isNullOrBlank() || role != null) {
        remember(initialUserName, role) {
            UserSession.set(initialUserName ?: sessionName, role ?: sessionRole)
        }
    }

    val navigator = LocalNavigator.currentOrThrow

    val rememberUserRole: Role = try {
        Role.valueOf(rememberedRoleName)
    } catch (e: Exception) {
        Role.STUDENT
    }

    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        color = Color.White
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),

            containerColor = Color.White,
            tonalElevation = 4.dp ,
            windowInsets = WindowInsets(0.dp)
        ) {
            fixedItems.forEachIndexed { index, item ->
                val selected = index == selectedIndex
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
                        onSelect?.invoke(index)

                        val nameToUse = rememberedUserName.ifBlank { (UserSession.name ?: "") }

                        when (index) {
                            0 -> {
                                navigator.push(
                                    AdminDashboard(
                                        nameToUse.ifBlank { "Usuario" },
                                        rememberUserRole
                                    ))
                            }
                            1 -> {
                                navigator.push(UsersScreen())
                            }
                            2 -> {
                                navigator.push(UnitsScreen())
                            }
                            3 -> {
                                navigator.push(ExercisesScreen(null))
                            }
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