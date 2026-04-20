package org.example.project.screens.admindScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.AppLayout
import org.example.project.dtos.CreateUserDto
import org.example.project.dtos.FilterUsersDto
import org.example.project.dtos.Role
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UserDto
import org.example.project.network.RepositoryProvider
import org.example.project.viewModel.UserViewModel
import org.example.project.network.UserSession

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
        val focusManager = LocalFocusManager.current

        var editing by remember { mutableStateOf<UserDto?>(null) }
        var confirmDelete by remember { mutableStateOf<UserDto?>(null) }
        // Changed: showAddUser logic now follows Units/Exercises pattern (toggle boolean for inline form)
        var isAddingUser by remember { mutableStateOf(false) }

        // Form fields state
        var newUserName by remember { mutableStateOf("") }
        var newUserEmail by remember { mutableStateOf("") }
        var newUserPassword by remember { mutableStateOf("") }
        var newUserRole by remember { mutableStateOf(Role.STUDENT) }
        var newUserUnit by remember { mutableStateOf<UnitDto?>(null) }

        // Dropdown states
        var roleMenuExpanded by remember { mutableStateOf(false) }
        var unitMenuExpanded by remember { mutableStateOf(false) }

        var showFilterOpcions by remember { mutableStateOf(false) }
        var filterUser by remember { mutableStateOf<FilterUsersDto?>(null) }

        var searchQuery by remember { mutableStateOf("") }
        var selectedIndex by remember { mutableStateOf(1) } // index in bottom bar

        // Estados para filtros y búsqueda aplicada
        var selectedRole by remember { mutableStateOf<Role?>(null) }
        var roleExpanded by remember { mutableStateOf(false) }

        var selectedUnit by remember { mutableStateOf<UnitDto?>(null) }
        var unitExpanded by remember { mutableStateOf(false) }

        // Variable para mostrar/ocultar formulario de filtro
        var isFiltering by remember { mutableStateOf(false) }

        var textSearch by remember { mutableStateOf("") }
        var searchUsers by remember { mutableStateOf(false) }

        // UI Text variables
        var textAdd by remember { mutableStateOf("") }
        var butonAddColor by remember { mutableStateOf(Color(0xFFB8F4C4)) }

        if (isAddingUser) {
            textAdd = "Cancelar creación"
            butonAddColor = Color(0xFFFFD4D4)
        } else {
            textAdd = "+ Agregar nuevo usuario"
            butonAddColor = Color(0xFFB8F4C4)
        }

        // usa la variable showFilterOpcions para abrir/cerrar el DropdownMenu
        var filterMenuExpanded by remember { mutableStateOf(false) }

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
                println("Error en UsersScreen: $it") // Log para debugging
            }
        }

        LaunchedEffect(searchQuery, selectedRole, selectedUnit?.id) {
            vm.getFilterUsers(
                FilterUsersDto(
                    name = searchQuery,
                    role = selectedRole,
                    unitId = selectedUnit?.id
                )
            )
        }


        // Usamos AppLayout que provee card de inicio y bottom bar fijo
        AppLayout(
            actualScreen = "Administrar Usuarios",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Card(
                modifier = Modifier
                    .fillMaxSize() // Cambiado de fillMaxWidth a fillMaxSize
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Cambiado de fillMaxWidth a fillMaxSize
                        .padding(horizontal = 12.dp, vertical = 20.dp),
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = MaterialTheme.shapes.small,
                            placeholder = {
                                Text(
                                    "Buscar usuarios...",
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize.value.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = "Buscar"
                                )
                            },
                            singleLine = true,
                        )

                        // Botón para alternar visibilidad del filtro (Formulario inline)
                        IconButton(
                            onClick = { isFiltering = !isFiltering },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isFiltering) Color(0xFFE0E0E0) else Color.White,
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtros",
                                tint = if (isFiltering) Color(0xFF003AB6) else Color(0xFF4A4A4A)
                            )
                        }
                    }

                    // Formulario de Filtros Expandible
                    AnimatedVisibility(
                        visible = isFiltering,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Filtros de Búsqueda",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )

                                // Filtros en fila o columna
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rol Dropdown
                                    Box(Modifier.weight(1f)) {
                                        ExposedDropdownMenuBox(
                                            expanded = roleExpanded,
                                            onExpandedChange = { roleExpanded = !roleExpanded }
                                        ) {
                                            OutlinedTextField(
                                                value = selectedRole?.name ?: "Todos los roles",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Rol", fontSize = 12.sp) },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = roleExpanded
                                                    )
                                                },
                                                modifier = Modifier.menuAnchor(
                                                    MenuAnchorType.PrimaryNotEditable,
                                                    enabled = true
                                                ).fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = roleExpanded,
                                                onDismissRequest = { roleExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Todos") },
                                                    onClick = {
                                                        selectedRole = null; roleExpanded = false
                                                    }
                                                )
                                                Role.entries.forEach { rol ->
                                                    DropdownMenuItem(
                                                        text = { Text(rol.name) },
                                                        onClick = {
                                                            selectedRole = rol; roleExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Unidad Dropdown
                                    Box(Modifier.weight(1f)) {
                                        ExposedDropdownMenuBox(
                                            expanded = unitExpanded,
                                            onExpandedChange = { unitExpanded = !unitExpanded }
                                        ) {
                                            OutlinedTextField(
                                                value = selectedUnit?.name ?: "Todas las unidades",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Unidad", fontSize = 12.sp) },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = unitExpanded
                                                    )
                                                },
                                                modifier = Modifier.menuAnchor(
                                                    MenuAnchorType.PrimaryNotEditable,
                                                    enabled = true
                                                ).fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = unitExpanded,
                                                onDismissRequest = { unitExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Todas") },
                                                    onClick = {
                                                        selectedUnit = null; unitExpanded = false
                                                    }
                                                )
                                                ui.unit.forEach { unit ->
                                                    DropdownMenuItem(
                                                        text = { Text(unit.name) },
                                                        onClick = {
                                                            selectedUnit = unit
                                                            unitExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        // Limpiar
                                        searchQuery = ""
                                        selectedRole = null
                                        selectedUnit = null
                                        vm.getFilterUsers(FilterUsersDto())
                                    }) {
                                        Text("Limpiar filtros")
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
                            onClick = { isAddingUser = !isAddingUser },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = butonAddColor
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                textAdd,
                                color = if (isAddingUser) Color(0xFF8B0000) else Color(0xFF2D5E3D)
                            )
                        }
                    }

                    // Formulario desplegable para agregar usuario
                    AnimatedVisibility(
                        visible = isAddingUser,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Nuevo Usuario",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                OutlinedTextField(
                                    value = newUserName,
                                    onValueChange = { newUserName = it },
                                    label = { Text("Nombre*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = newUserName.isBlank()
                                )

                                OutlinedTextField(
                                    value = newUserEmail,
                                    onValueChange = { newUserEmail = it },
                                    label = { Text("Email*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = newUserEmail.isBlank()
                                )

                                OutlinedTextField(
                                    value = newUserPassword,
                                    onValueChange = { newUserPassword = it },
                                    label = { Text("Contraseña*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = newUserPassword.isBlank(),
                                    // Visual transformation could be added here if needed
                                )

                                // Role Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = roleMenuExpanded,
                                    onExpandedChange = { roleMenuExpanded = !roleMenuExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = newUserRole.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Rol") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor(
                                                MenuAnchorType.PrimaryNotEditable,
                                                enabled = true
                                            )
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = roleMenuExpanded,
                                        onDismissRequest = { roleMenuExpanded = false }
                                    ) {
                                        Role.entries.forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text(role.name) },
                                                onClick = {
                                                    newUserRole = role
                                                    roleMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Unit Dropdown
                                if (ui.unit.isNotEmpty() && newUserUnit == null) {
                                    newUserUnit = ui.unit.first()
                                }

                                ExposedDropdownMenuBox(
                                    expanded = unitMenuExpanded,
                                    onExpandedChange = { unitMenuExpanded = !unitMenuExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = newUserUnit?.name ?: "Selecciona unidad",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Unidad Inicial") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor(
                                                MenuAnchorType.PrimaryNotEditable,
                                                enabled = true
                                            )
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = unitMenuExpanded,
                                        onDismissRequest = { unitMenuExpanded = false }
                                    ) {
                                        ui.unit.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit.name) },
                                                onClick = {
                                                    newUserUnit = unit
                                                    unitMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    androidx.compose.material3.Button(
                                        onClick = { isAddingUser = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFD4D4)
                                        )
                                    ) {
                                        Text("Cancelar", color = Color(0xFF8B0000))
                                    }

                                    androidx.compose.material3.Button(
                                        onClick = {
                                            if (newUserName.isBlank() || newUserEmail.isBlank() || newUserPassword.isBlank()) {
                                                // Handle error (show snackbar ideally via callback or local state)
                                                return@Button
                                            }

                                            vm.registerUser(
                                                CreateUserDto(
                                                    name = newUserName,
                                                    email = newUserEmail,
                                                    password = newUserPassword,
                                                    role = newUserRole,
                                                    currentUnitId = newUserUnit?.id
                                                )
                                            )

                                            // Reset and close
                                            newUserName = ""
                                            newUserEmail = ""
                                            newUserPassword = ""
                                            newUserRole = Role.STUDENT
                                            newUserUnit = null
                                            isAddingUser = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFB8F4C4)
                                        )
                                    ) {
                                        Text("Guardar Usuario", color = Color(0xFF2D5E3D))
                                    }
                                }
                            }
                        }
                    }

                    if (ui.isLoading) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (ui.users.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            Text(
                                "No hay usuarios disponibles.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Cambiado de Column con verticalScroll a LazyColumn con weight
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ui.users) { user ->
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
                            }
                        }
                    }
                }
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
        Modifier.fillMaxWidth().clickable { onClick() }
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        if (user.role == Role.ADMIN) {
                            Badge(containerColor = BadgeDefaults.containerColor) {
                                Text(user.role.name.lowercase(), fontSize = 12.sp)
                            }
                        } else {
                            Badge(containerColor = Color(0xFFB8F4C4)) {
                                user.role?.let { Text(it.name.lowercase(), fontSize = 12.sp) }
                            }
                        }
                    }
                    Text("Nivel: $levelName", style = MaterialTheme.typography.labelSmall)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    user.name.take(1).let {
                        Text(
                            it.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A),
                        )
                    }
                }

            }
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