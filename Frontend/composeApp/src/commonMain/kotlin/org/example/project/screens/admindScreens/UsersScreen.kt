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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.Role
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UserDto
import org.example.project.network.RepositoryProvider
import org.example.project.viewModel.UserViewModel
import org.example.project.components.NavItem
import org.example.project.components.ReusableBottomBar
import org.example.project.dtos.FilterUserDto
import org.example.project.dtos.TestType
import org.example.project.network.UserSession
import org.jetbrains.compose.resources.Font

class UsersScreen : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val vm = rememberScreenModel {
            UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo)
        }

        val ui by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow

        var editing by remember { mutableStateOf<UserDto?>(null) }
        var confirmDelete by remember { mutableStateOf<UserDto?>(null) }
        var showAddUser by remember { mutableStateOf(false) }

        var showFilterOpcions by remember { mutableStateOf(false) }
        var filterUser by remember { mutableStateOf<FilterUserDto?>(null) }

        var searchQuery by remember { mutableStateOf("") }
        var selectedIndex by remember { mutableStateOf(1) } // index in bottom bar

        // Estados para filtros y búsqueda aplicada
        var selectedRole by remember { mutableStateOf<Role?>(null) }
        var roleExpanded by remember { mutableStateOf(false) }

        var selectedUnit by remember { mutableStateOf<UnitDto?>(null) }
        var unitExpanded by remember { mutableStateOf(false) }

        var appliedSearch by remember { mutableStateOf("") }
        var appliedRole by remember { mutableStateOf<Role?>(null) }
        var appliedUnitId by remember { mutableStateOf<Int?>(null) }

        // usa la variable showFilterOpcions para abrir/cerrar el DropdownMenu
        var filterMenuExpanded by remember { mutableStateOf(false) }

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }

        // Usamos AppLayout que provee card de inicio y bottom bar fijo
        AppLayout(
            actualScreen = "Administrar usuarios",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8F0))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar usuarios...", fontSize = MaterialTheme.typography.bodyMedium.fontSize.value.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    // Al presionar enviar aplicamos la búsqueda y filtros actuales
                                    appliedSearch = searchQuery
                                    appliedRole = selectedRole
                                    appliedUnitId = selectedUnit?.id
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Buscar enviar")
                                }
                            }
                        )

                        // Icono de filtros con DropdownMenu anclado
                        Box {
                            IconButton(
                                onClick = { filterMenuExpanded = !filterMenuExpanded },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = MaterialTheme.shapes.small)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtros",
                                    tint = Color(0xFF4A4A4A)
                                )
                            }

                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = { filterMenuExpanded = false },
                                modifier = Modifier
                                    .width(320.dp)
                                    .background(Color.White)
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Filtros", fontWeight = FontWeight.SemiBold)

                                    // Rol
                                    ExposedDropdownMenuBox(
                                        expanded = roleExpanded,
                                        onExpandedChange = { roleExpanded = !roleExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedRole?.name ?: "Todos los roles",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Rol") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                            Role.entries.forEach { rol ->
                                                DropdownMenuItem(
                                                    text = { Text(rol.name) },
                                                    onClick = {
                                                        selectedRole = rol
                                                        roleExpanded = false
                                                    }
                                                )
                                            }
                                            // opción para limpiar rol
                                            DropdownMenuItem(
                                                text = { Text("Todos") },
                                                onClick = {
                                                    selectedRole = null
                                                    roleExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    // Unidad
                                    ExposedDropdownMenuBox(
                                        expanded = unitExpanded,
                                        onExpandedChange = { unitExpanded = !unitExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedUnit?.name ?: "Todas las unidades",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Unidad actual") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                            ui.unit.forEach { level ->
                                                DropdownMenuItem(
                                                    text = { Text(level.name) },
                                                    onClick = {
                                                        selectedUnit = level
                                                        unitExpanded = false
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Todas") },
                                                onClick = {
                                                    selectedUnit = null
                                                    unitExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    // Acciones: Aplicar / Limpiar
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            // Limpiar selecciones y aplicados
                                            selectedRole = null
                                            selectedUnit = null
                                            appliedRole = null
                                            appliedUnitId = null
                                            appliedSearch = ""
                                            searchQuery = ""
                                            filterMenuExpanded = false
                                        }) {
                                            Text("Limpiar")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        TextButton(onClick = {
                                            // Aplicar filtros actuales
                                            appliedRole = selectedRole
                                            appliedUnitId = selectedUnit?.id
                                            appliedSearch = searchQuery
                                            filterMenuExpanded = false
                                        }) {
                                            Text("Aplicar")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Botones (Agregar / Orden) - la acción de agregar también está en FAB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { showAddUser = true },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB8F4C4)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "+ Agregar nuevo usuario",
                                color = Color(0xFF2D5E3D)
                            )
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = { /* Ordenar */ },
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text("Orden")
                        }
                    }

                    // Lista de usuarios filtrada por searchQuery y filtros aplicados
                    val filtered = ui.users.filter { user ->
                        val matchesSearch = appliedSearch.isBlank() ||
                                user.name.contains(appliedSearch, ignoreCase = true) ||
                                user.email.contains(appliedSearch, ignoreCase = true)
                        val matchesRole = appliedRole == null || user.role == appliedRole
                        val matchesUnit = appliedUnitId == null || user.currentUnitId == appliedUnitId
                        matchesSearch && matchesRole && matchesUnit
                    }

                    if (ui.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (filtered.isEmpty()) {
                        Text(
                            "No hay usuarios disponibles.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        filtered.forEach { user ->
                            UserCard(
                                user = user,
                                onClick = {
                                    user.id?.let {
                                        navigator.push(UserDetailsScreen(userId = it))
                                    } ?: run { /* manejar error */ }
                                },
                                onEdit = { editing = user },
                                levels = ui.unit,
                                onDelete = { confirmDelete = user }
                            )
                            Spacer(Modifier.height(12.dp))

                        }
                    }
                }
            }


            // Add user dialog
            if (showAddUser && !ui.isLoading) {
                EditUser(
                    initial = UserDto(
                        id = null,
                        name = "",
                        email = "",
                        password = "",
                        role = Role.STUDENT,
                        currentUnitId = ui.unit.firstOrNull()?.id,
                        provider = "",
                        preferences = "",
                        activeNow = false,
                        createdAt = ""
                    ),
                    levels = ui.unit,
                    onSave = { newUser ->
                        vm.registerUser(
                            CreateUserDto(
                                name = newUser.name,
                                email = newUser.email,
                                password = newUser.password ?: "",
                                role = newUser.role,
                                currentUnitId = newUser.currentUnitId
                            )
                        )
                        showAddUser = false
                    },
                    onDismiss = { showAddUser = false }
                )
            }

        }
    }
}

@Composable
fun UserCard(
    user: UserDto,
    levels: List<UnitDto>,
    onClick: () -> Unit = {},
    onEdit: (UserDto) -> Unit,
    onDelete: (UserDto) -> Unit
) {
    val levelName = levels.find { it.id == user.currentUnitId }?.name ?: "No asignado"

    Card(
        Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
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

            }

            Spacer(Modifier.height(8.dp))
            Text("Nivel: $levelName", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text("Rol: ${user.role}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditUser(
    initial: UserDto,
    levels: List<UnitDto>,
    onSave: (UserDto) -> Unit,
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
    var selectedUnit = levels.find { it.id == initial.currentUnitId }

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
                        value = selectedUnit?.name ?: "Selecciona una unidad",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad") },
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
                                    selectedUnit = level
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
                            preferences = null,
                            role = role,
                            currentUnitId = selectedUnit?.id,


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