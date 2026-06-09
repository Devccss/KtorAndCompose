package org.example.project.screens.editorScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
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
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import kotlinx.coroutines.launch
import org.example.project.components.EditorLayout
import org.example.project.components.UserStatisticsSection
import org.example.project.dtos.Role
import org.example.project.dtos.UpdateUserDto
import org.example.project.dtos.UserDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.screens.LoginScreen
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import org.jetbrains.compose.resources.Font

class EditorUserDetailsScreen(private val userId: Int) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo)
        }
        val unitVm = rememberScreenModel {
            UnitViewModel(RepositoryProvider.unitRepo)
        }

        val ui by vm.state.collectAsState()
        val unitUi by unitVm.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Fuentes
        val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
        val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

        var confirmDelete by remember { mutableStateOf<UserDto?>(null) }
        // Estado de Edición Inline
        var isEditing by remember { mutableStateOf(false) }

        // Campos Editables
        var user by remember { mutableStateOf(ui.currentUser) }
        var editedName by remember { mutableStateOf("") }
        var editedEmail by remember { mutableStateOf("") }
        var editedRole by remember { mutableStateOf<Role?>(Role.STUDENT) }
        var editedUnitId by remember { mutableStateOf<Int?>(null) }
        
        // Logica para Dropdowns
        var roleMenuExpanded by remember { mutableStateOf(false) }
        var unitMenuExpanded by remember { mutableStateOf(false) }
        val isOwnProfile = UserSession.idUser == userId && userId > 0

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }

        // Sincronizar estados locales con el usuario cargado
        LaunchedEffect(userId) {
            if (userId > 0 ) {
                vm.getUserById(userId)
                vm.loadUserStatistics(userId)
            }
        }

        // NUEVO: Actualizar el estado local cuando llega el usuario del VM
        LaunchedEffect(ui.currentUser) {
            ui.currentUser?.let { loadedUser ->
                if (loadedUser.id == userId) {
                    user = loadedUser
                    editedName = loadedUser.name
                    editedEmail = loadedUser.email
                    editedRole = loadedUser.role ?: Role.STUDENT
                    editedUnitId = loadedUser.currentUnitId
                }
            }
        }

        EditorLayout(
            actualScreen = "Detalles del alumno",
            selectedIndex = -1,
            onSelect = { },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {

                if (ui.isLoading && user == null) {
                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando usuario...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (user == null) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Usuario no encontrado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            Spacer(Modifier.width(8.dp))
                            Text("Volver")
                        }
                    }
                } else {
                    // Contenido Principal
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // --- HEADER CARD: Avatar, Datos Principales y Botones de Acción ---
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                user?.name?.take(1)?.let {
                                    Text(
                                        it.uppercase(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A4A4A),
                                        fontFamily = encodeSansFamily
                                    )
                                } ?: "Usuario sin nombre"
                            }

                            Spacer(Modifier.width(16.dp))

                            // Columna Central: Campos Editables
                            Column(Modifier.weight(1f)) {
                                // Nombre
                                BasicTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    readOnly = !isEditing,
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = encodeSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF131313)
                                    ),
                                    modifier = if (isEditing) {
                                        Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                Color(0xFFE0E0E0),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(6.dp)
                                    } else {
                                        Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                    }
                                )

                                Spacer(Modifier.height(4.dp))

                                // Rol y Email
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Dropdown de Rol (Editable) o Badge (Lectura)
                                    if (isEditing) {
                                        ExposedDropdownMenuBox(
                                            expanded = roleMenuExpanded,
                                            onExpandedChange = {
                                                roleMenuExpanded = !roleMenuExpanded
                                            }
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .menuAnchor(
                                                        MenuAnchorType.PrimaryNotEditable,
                                                        enabled = true
                                                    )
                                                    .height(28.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF0F0F0),
                                                border = null
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        editedRole?.name ?: "Sin Rol",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Icon(
                                                        if (roleMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        "Expandir", modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            ExposedDropdownMenu(
                                                expanded = roleMenuExpanded,
                                                onDismissRequest = { roleMenuExpanded = false }
                                            ) {
                                                Role.entries.forEach { role ->
                                                    DropdownMenuItem(
                                                        text = { Text(role.name) },
                                                        onClick = {
                                                            editedRole = role
                                                            roleMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Badge(
                                            containerColor = Color(0xFFE0E0E0),
                                            contentColor = Color(0xFF4A4A4A)
                                        ) {
                                            Text(user?.role?.name ?: "Estudiante", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Email Field
                                BasicTextField(
                                    value = editedEmail,
                                    onValueChange = { editedEmail = it },
                                    readOnly = !isEditing,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = jetbrainsMonoFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    ),
                                    modifier = if (isEditing) {
                                        Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                Color(0xFFE0E0E0),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(6.dp)
                                    } else {
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    }
                                )
                            }

                            // Columna Derecha: Botones
                            Column(horizontalAlignment = Alignment.End) {
                                if (isOwnProfile) {
                                    IconButton(
                                        onClick = {
                                            navigator.replaceAll(LoginScreen(logout = true))
                                        },
                                        modifier = Modifier.size(36.dp)
                                            .background(Color(0xFFFFEFEF), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            "Deslogearse",
                                            tint = Color(0xFFB00020),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // --- ESTADÍSTICAS Y PROGRESO ---
                        Text(
                            "Progreso Académico",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = encodeSansFamily,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Fila de Progreso y Unidad Actual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Card Unidad Actual
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FFF3)),
                                border = if (isEditing) BorderStroke(
                                    1.dp,
                                    Color(0xFF2D5E3D)
                                ) else null
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.School,
                                            null,
                                            tint = Color(0xFF2D5E3D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Unidad Actual",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF2D5E3D)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    if (isEditing) {
                                        // Dropdown para cambiar unidad
                                        ExposedDropdownMenuBox(
                                            expanded = unitMenuExpanded,
                                            onExpandedChange = {
                                                unitMenuExpanded = !unitMenuExpanded
                                            }
                                        ) {
                                            val currentUnitName =
                                                ui.unit.find { it.id == editedUnitId }?.name
                                                    ?: "Sin asignar"

                                            OutlinedTextField(
                                                value = currentUnitName,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.menuAnchor(
                                                    MenuAnchorType.PrimaryNotEditable,
                                                    enabled = true
                                                ).fillMaxWidth(),
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = unitMenuExpanded
                                                    )
                                                },
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = unitMenuExpanded,
                                                onDismissRequest = { unitMenuExpanded = false }
                                            ) {
                                                unitUi.units.forEach { unit ->
                                                    DropdownMenuItem(
                                                        text = { Text(unit.name) },
                                                        onClick = {
                                                            editedUnitId = unit.id
                                                            unitMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        val currentUnitName =
                                            ui.unit.find { it.id == editedUnitId }?.name
                                                ?: "Sin asignar"
                                        Text(
                                            currentUnitName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = encodeSansFamily
                                        )
                                    }
                                }
                            }

                            // Card Progreso (Calculado)
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9EA))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Progreso Global",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFB08C00)
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Calcular progreso basado en la posición de la unidad
                                    val totalUnits = unitUi.units.size
                                    val completedUnitsSize = unitUi.unitsCompleted.size
                                    val progress = if (totalUnits > 0 && completedUnitsSize >= 0) {
                                        completedUnitsSize.toFloat() / totalUnits
                                    } else 0f

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFFFFD700),
                                        trackColor = Color(0xFFFFE0B2),
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${(progress * 100).toInt()}% Completado",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        UserStatisticsSection(
                            state = ui,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // --- INFORMACIÓN ADICIONAL ---
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Información del Sistema",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Gray
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Proveedor:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                    Text(
                                        user?.provider ?: "Local",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Miembro desde:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                    Text(
                                        user?.createdAt ?: "Desconocido",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Estado Actual:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                    Text(
                                        if (user?.activeNow == true) "🟢 Online" else "⚫ Offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = jetbrainsMonoFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dialogo de Confirmación de Borrado
            confirmDelete?.let { u ->
                AlertDialog(
                    onDismissRequest = { confirmDelete = null },
                    containerColor = Color.White,
                    title = { Text("Eliminar Usuario") },
                    text = { Text("¿Seguro que deseas eliminar a ${u.name}? Esta acción eliminará todo su progreso y no se puede deshacer.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                u.id?.let { id ->
                                    vm.deleteUser(id)
                                    confirmDelete = null
                                    navigator.pop()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8B0000))
                        ) { Text("Eliminar definitivamente") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}