package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import org.example.project.models.Level
import org.example.project.models.DifficultyLevel
import org.example.project.viewModel.LevelsViewModel

class AdminLevelsScreen : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            LevelsViewModel(RepositoryProvider.levelRepository)
        }

        val ui by vm.state.collectAsState()
        var editing by remember { mutableStateOf<Level?>(null) }
        var confirmDelete by remember { mutableStateOf<Level?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }
        var insertBeforeId by remember { mutableStateOf<Int?>(null) }
        var insertAfterId by remember { mutableStateOf<Int?>(null) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

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
                        currentPage = "levels",
                        titlePage = "Gestión de Niveles",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )

                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(16.dp).padding(WindowInsets.safeDrawing.asPaddingValues())) {
                    Text("Gestión de Niveles", style = MaterialTheme.typography.headlineMedium)
                    Text("Administra los niveles y evaluaciones", style = MaterialTheme.typography.bodySmall)
                }
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    when {
                        ui.isLoading -> CircularProgressIndicator()
                        ui.error != null -> Text("Error: ${ui.error}")
                        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                            val levels = ui.levels

                            items(levels.size + 1) { index ->
                                // Si estamos al final de la lista, no hay siguiente
                                val before = levels.getOrNull(index - 1)
                                val after = levels.getOrNull(index)

                                AddButton(onClick = {
                                    insertBeforeId = before?.id
                                    insertAfterId = after?.id
                                    showAddDialog = true
                                })

                                after?.let { level ->
                                    LevelCard(
                                        level = level,
                                        onEdit = { editing = it },
                                        onDelete = { confirmDelete = it },
                                        onClick = { navigator.push(LevelDetails(level.id)) }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                    println("Mensaje de error: ${ui.error}")
                }

                editing?.let {
                    EditLevelDialog(
                        initial = it,
                        onSave = {
                            vm.addLevel(it, insertBeforeId, insertAfterId)
                            showAddDialog = false
                            insertBeforeId = null
                            insertAfterId = null
                        },
                        onDismiss = { editing = null }
                    )
                }

                if (showAddDialog) {
                    EditLevelDialog(
                        initial = Level(null, accent = 0, difficulty = DifficultyLevel.A1, name = "", description = "", orderLevel = 0f),
                        onSave = {
                            vm.addLevel(it, insertBeforeId, insertAfterId)
                            showAddDialog = false
                            insertBeforeId = null
                            insertAfterId = null
                        },
                        onDismiss = { showAddDialog = false }
                    )
                }

                confirmDelete?.let {
                    AlertDialog(
                        onDismissRequest = { confirmDelete = null },
                        title = { Text("Eliminar nivel") },
                        text = { Text("¿Seguro de eliminar ${it.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                it.id?.let(vm::delete)
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
fun AddButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(onClick = onClick) {
            Icon(Icons.Default.Add, contentDescription = "Agregar nivel")
        }
    }
}


@Composable
fun LevelCard(level: Level, onEdit: (Level) -> Unit, onDelete: (Level) -> Unit, onClick: () -> Unit) {
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
                    Text("${level.accent}", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(level.name, style = MaterialTheme.typography.titleMedium)
                    Text("Dificultad: ${level.difficulty}", style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = { onEdit(level) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onDelete(level) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Descripción: ${level.description}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLevelDialog(initial: Level, onSave: (Level) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var desc by remember { mutableStateOf(initial.description) }
    var difficulty by remember { mutableStateOf(initial.difficulty) }

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Nivel" else "Editar Nivel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                OutlinedTextField(desc, { desc = it }, label = { Text("Descripción") })

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = difficulty.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificultad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DifficultyLevel.values().forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.name) },
                                onClick = {
                                    difficulty = level
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
                onSave(initial.copy(name = name, description = desc, difficulty = difficulty))
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

