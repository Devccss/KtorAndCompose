package org.example.project.screens.admindScreens

import RepositoryProvider
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateUserDto
import org.example.project.models.Level
import org.example.project.models.Role
import org.example.project.models.Test
import org.example.project.models.TestType
import org.example.project.models.Users
import org.example.project.viewModel.UserViewModel

class UsersScreen : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {

        val vm = rememberScreenModel {
            UserViewModel(RepositoryProvider.usersRepository, RepositoryProvider.levelRepository)
        }

        val ui by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

        var editing by remember { mutableStateOf<Users?>(null) }
        var confirmDelete by remember { mutableStateOf<Users?>(null) }
        var showAddUser by remember { mutableStateOf(false) }

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }


        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navigator.push(route)
                })
            }
        ) {
            Scaffold(
                topBar = {
                    AdminTopBar(
                        currentPage = "Users",
                        titlePage = "Gestion de Usuarios",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )

                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddUser = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Test")
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    when {
                        ui.isLoading -> CircularProgressIndicator()
                        ui.error != null -> Text("Error: ${ui.error}")
                        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                            if (ui.users.isEmpty()) {
                                item {
                                    Text(
                                        "No hay usuarios disponibles.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            items(ui.users) { user ->
                                UserCard(
                                    user = user,
                                    onClick = {
                                        if (user.id != null) {
                                            navigator.push(
                                                UserDetailsScreen(userId = user.id)
                                            )
                                        } else {
                                            ui.error = "ID de Test no disponible"
                                        }

                                    },
                                    onEdit = { editing = user },
                                    levels = ui.levels,
                                    onDelete = { confirmDelete = user }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }


                editing?.let { user ->
                    EditUser(
                        initial = user,
                        onSave = { updated ->
                            updated.id?.let {
                                user.id?.let { vm.updateUser(it, updated) }
                            }
                            editing = null
                        },
                        levels = ui.levels,
                        onDismiss = { editing = null }
                    )
                }


                if (showAddUser && !ui.isLoading ) {
                    EditUser(
                        initial = Users(
                            name = "",
                            email = "",
                            password = "",
                            role = Role.STUDENT,
                            currentLevelId = ui.levels.firstOrNull()?.id
                        ),
                        levels = ui.levels,
                        onSave = { newUser ->
                            vm.registerUser(
                                CreateUserDto(
                                    name = newUser.name,
                                    email = newUser.email,
                                    password = newUser.password ?: "",
                                    role = newUser.role,
                                    currentLevelId = newUser.currentLevelId
                                )
                            )
                            showAddUser = false

                        },
                        onDismiss = { showAddUser = false }
                    )
                }

                // Eliminar diálogo
                confirmDelete?.let { user ->
                    AlertDialog(
                        onDismissRequest = { confirmDelete = null },
                        title = { Text("Eliminar Usuario") },
                        text = { Text("¿Seguro de eliminar este Usuario: ${user.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                user.id?.let { vm.deleteUser(it) }
                                confirmDelete = null
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
}

@Composable
fun UserCard(
    user: Users,
    levels: List<Level>,
    onClick: () -> Unit = {},
    onEdit: (Users) -> Unit,
    onDelete: (Users) -> Unit
) {
    val levelName = levels.find { it.id == user.currentLevelId }?.name ?: "No asignado"
    Card(
        Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF003AB6), Color(0xFF48145B))
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.id?.toString() ?: "N", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Row {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                }
                IconButton(
                    onClick = { onEdit(user) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { onDelete(user) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Nivel: $levelName", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text("Rol: ${user.role}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUser(
    initial: Users,
    levels: List<Level>,
    onSave: (Users) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var email by remember { mutableStateOf(initial.email) }
    val password by remember { mutableStateOf(initial.password?.isNotEmpty()) }
    var newPassword by remember { mutableStateOf("") }
    var changePassword by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val roles by remember { mutableStateOf(Role.entries) }
    var role by remember { mutableStateOf(initial.role) }
    var roleExpanded by remember { mutableStateOf(false) }
    var selectedLevel = levels.find { it.id == initial.currentLevelId }

    val testTypes = TestType.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })

                OutlinedTextField(email, { email = it }, label = { Text("Email") })


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLevel?.name ?: "Selecciona un nivel",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nivel") },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        levels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.name) },
                                onClick = {
                                    selectedLevel = level
                                    expanded = false
                                }
                            )
                        }
                    }

                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,

                    ) {

                    if (password?.and(!changePassword) == true) {
                        Card() {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Contraseña")
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    Modifier
                                        .background(
                                            Color(0xFF60D16D)
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        "Asignada",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }


                        TextButton(
                            onClick = { changePassword = !changePassword },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                "Cambiar",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else {
                        OutlinedTextField(
                            newPassword,
                            { newPassword = it },
                            label = { Text("Nueva Contraseña") })
                    }


                }

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role?.name ?: roles[2].name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.name) },
                                onClick = {
                                    role = rol
                                    roleExpanded = false
                                }
                            )
                        }
                    }

                }

            }
        },
        confirmButton = {
            TextButton(onClick = {

                if (name.isNotBlank()) {
                    onSave(
                        initial.copy(
                            name = name,
                            email = email,
                            password = newPassword,
                            provider = null,
                            providerId = null,
                            preferences = null,
                            role = role,
                            currentLevelId = selectedLevel?.id,


                            )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}