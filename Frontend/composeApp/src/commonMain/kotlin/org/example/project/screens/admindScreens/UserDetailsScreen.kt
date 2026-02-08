package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import kotlinx.coroutines.launch
import org.example.project.components.AppLayout
import org.example.project.components.NavItem
import org.example.project.components.ReusableBottomBar
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UserDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.UserViewModel
import org.jetbrains.compose.resources.Font

class UserDetailsScreen(private val userId: Int) : Screen {
    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo)
        }
        val ui by vm.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var editing by remember { mutableStateOf<UserDto?>(null) }
        var confirmDelete by remember { mutableStateOf<UserDto?>(null) }
        var selectedIndex by remember { mutableStateOf(1) } // bottom bar index

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }

        // Try to find the user in current state
        val user = ui.users.find { it.id == userId }

        AppLayout(
            actualScreen = "Administrar Unidades",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
            snackbarHostState = snackbarHostState
        ) { _,_,_ ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8F0))
                    .padding(16.dp),

            ) {

                if (ui.isLoading && user == null) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando usuario...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (user == null) {
                    // Usuario no encontrado
                    Column(
                        Modifier
                            .fillMaxSize()
                            .wrapContentHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Usuario no encontrado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.material3.Button(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            Spacer(Modifier.width(8.dp))
                            Text("Volver")
                        }
                    }
                } else {
                    // Main content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header card: avatar, name, email, role, actions
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Avatar box
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

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(8.dp))
                                        Badge(containerColor = BadgeDefaults.containerColor) {
                                            user.role?.let { Text(it.name, fontSize = 12.sp) }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                                }

                                // Action buttons
                                IconButton(onClick = { editing = user }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { confirmDelete = user }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }

                        // Stats row
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FFF3))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Unidades asignada", style = MaterialTheme.typography.labelSmall)
                                    Spacer(Modifier.height(6.dp))
                                    val currentUnitName = ui.unit.find { it.id == user.currentUnitId }?.name ?: "Ninguna"
                                    Text(currentUnitName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9EA))
                            ) {
                                /*Column(Modifier.padding(12.dp)) {
                                    Text("Unidades completadas", style = MaterialTheme.typography.labelSmall)
                                    Spacer(Modifier.height(6.dp))
                                    // Placeholder: si en el state hay progreso, reemplazar por conteo real
                                    val completed = ui.progress?.count { it.userId == user.id } ?: 0
                                    Text(completed.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }*/
                            }
                        }

                        // Additional info card
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FF))
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Información", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                Text("Proveedor: ${user.provider ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                Text("Activo ahora: ${if (user.activeNow == true) "Sí" else "No"}", style = MaterialTheme.typography.bodySmall)
                                Text("Creado: ${user.createdAt ?: "Desconocido"}", style = MaterialTheme.typography.bodySmall)
                            }
                        }


                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Edit dialog
            editing?.let { u ->
                EditUser(
                    initial = u,
                    levels = ui.unit,
                    onSave = { updated ->
                        updated.id?.let { id -> vm.updateUser(id, updated) }
                        editing = null
                        // refresh or show snackbar
                        scope.launch { snackbarHostState.showSnackbar("Usuario actualizado") }
                    },
                    onDismiss = { editing = null }
                )
            }

            // Delete confirm
            confirmDelete?.let { u ->
                AlertDialog(
                    onDismissRequest = { confirmDelete = null },
                    title = { Text("Eliminar Usuario") },
                    text = { Text("¿Seguro de eliminar a ${u.name}? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            u.id?.let { id ->
                                vm.deleteUser(id)
                                confirmDelete = null
                                // volver atrás después de borrar
                                navigator.pop()
                            }
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}
