package org.example.project.screens.admindScreens

import RepositoryProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import org.example.project.models.Dialog
import org.example.project.models.DifficultyLevel
import org.example.project.models.Level
import org.example.project.viewModel.DialogViewModel

class DialogsScreen(private val levelId: Int?) : Screen {
    override val key = uniqueScreenKey
    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            DialogViewModel(RepositoryProvider.dialogsRepository)
        }
        val ui by vm.state.collectAsState()
        var editing by remember { mutableStateOf<Dialog?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf<Dialog?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(ui.error){

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
                        currentPage = "dialogs",
                        titlePage = "Administrar Diálogos",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar diálogo")
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            )  { padding ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    when {
                        ui.isLoading -> CircularProgressIndicator()
                        ui.error != null -> Text("Error: ${ui.error}")
                        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                            items(ui.dialogs) { dialog ->
                                DialogCard(
                                    dialog = dialog,
                                    onClick = {
                                        if (dialog.id != null) {
                                            navigator.push(
                                                DialogDetails(
                                                    dialogId = dialog.id,
                                                )
                                            )
                                        }else {
                                            ui.error = "ID de diálogo no disponible"
                                        }

                                    },
                                    onEdit = { editing = it },
                                    levels = ui.levels,
                                    onDelete = { confirmDelete = it }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Editar diálogo
                editing?.let { dialog ->
                    EditDialogDialog(
                        initial = dialog,
                        levels = ui.levels, // Asegúrate de pasar la lista de niveles
                        onSave = { updated ->
                            updated.id?.let { id ->
                                vm.updateDialog(id, updated)
                            }
                            editing = null
                        },
                        onDismiss = { editing = null }
                    )
                }

                // Agregar diálogo
                if (showAddDialog) {
                    EditDialogDialog(
                        initial = Dialog(
                            id = null,
                            levelId = levelId ?: 0,
                            name = "",
                            difficulty = DifficultyLevel.A1,
                            description = "",
                            audioUrl = "",
                            isActive = true,
                            createdAt = null
                        ),
                        levels = ui.levels,
                        onSave = { newDialog ->
                            println("EditDialogDialog: onSave llamado con $newDialog")
                            println("EditDialogDialog: Llamando a vm.addDialog con levelId=${newDialog.levelId}")
                            vm.addDialog(newDialog, newDialog.levelId)
                            showAddDialog = false
                        },
                        onDismiss = { showAddDialog = false }
                    )
                }

                // Eliminar diálogo
                confirmDelete?.let { dialog ->
                    AlertDialog(
                        onDismissRequest = { confirmDelete = null },
                        title = { Text("Eliminar diálogo") },
                        text = { Text("¿Seguro de eliminar este diálogo?") },
                        confirmButton = {
                            TextButton(onClick = {
                                dialog.id?.let(vm::deleteDialog)
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
fun DialogCard(
    dialog: Dialog,
    levels: List<Level>,
    onClick: () -> Unit = {},
    onEdit: (Dialog) -> Unit,
    onDelete: (Dialog) -> Unit
) {
    val levelName = levels.find { it.id == dialog.levelId }?.name ?: "Desconocido"
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
                    Text(dialog.id?.toString() ?: "N", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(dialog.name, style = MaterialTheme.typography.titleMedium)
                    Text("Nivel: $levelName", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "Dificultad: ${dialog.difficulty}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = { onEdit(dialog) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onDelete(dialog) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Descripción: ${dialog.description}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialogDialog(
    initial: Dialog,
    levels: List<Level>, // Debes pasar la lista de niveles aquí
    onSave: (Dialog) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var levelId by remember { mutableStateOf(initial.levelId) }
    var expanded by remember { mutableStateOf(false) }

    val selectedLevel = levels.find { it.id == levelId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Diálogo" else "Editar Diálogo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                OutlinedTextField(
                    description,
                    { description = it },
                    label = { Text("Descripción") })
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
                                    levelId = level.id ?: 0
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                println("EditDialogDialog: Botón Guardar presionado. name=$name, levelId=$levelId")
                if (name.isNotBlank()) {
                    onSave(initial.copy(name = name, description = description, levelId = levelId))
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
