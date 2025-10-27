package org.example.project.screens.admindScreens

import RepositoryProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.models.DifficultyLevel
import org.example.project.models.Level
import org.example.project.viewModel.LevelDetailsViewModel

class LevelDetails(
    private val levelId: Int?,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            LevelDetailsViewModel(
                levelId = levelId,
                levelRepository = RepositoryProvider.levelRepository,
                dialogsRepository = RepositoryProvider.dialogsRepository
            )
        }
        val ui by vm.state.collectAsState()
        var editLevel by remember { mutableStateOf<Level?>(null) }
        var confirmDelete by remember { mutableStateOf<Level?>(null) }
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val navigator = LocalNavigator.currentOrThrow
        var insertBeforeId by remember { mutableStateOf<Int?>(null) }
        var insertAfterId by remember { mutableStateOf<Int?>(null) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navigator.push(route)
                    }
                )
            }) {
            Scaffold(
                topBar = {
                    AdminTopBar(
                        currentPage = "LevelDetails",
                        titlePage = "Detalles del Nivel",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                ) {
                    Text("Detalles del Nivel", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.padding(8.dp))

                    ui.level?.let { lvl ->
                        Card(
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row {
                                        Text(
                                            "Nombre: ${lvl.name}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Card(
                                            modifier = Modifier
                                                .padding(end = 4.dp, bottom = 4.dp)
                                                .background(Color(220, 220, 220)),
                                            shape = MaterialTheme.shapes.small,
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Text(
                                                text = (if (lvl.isActive) "Activo" else "Inactivo"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (lvl.isActive) Color(
                                                    34,
                                                    139,
                                                    34
                                                ) else Color(178, 34, 34),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Descripción: ${lvl.description}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Dificultad: ${lvl.difficulty}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Nivel asociado: ${ui.level?.name ?: "Desconocido"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        { editLevel = ui.level},
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",

                                            )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        { confirmDelete = ui.level },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",

                                            )
                                    }
                                }
                            }
                        }

                        // Sección de diálogos asociados
                        Text("Diálogos asociados", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(4.dp))
                        if (ui.dialogs.isEmpty()) {
                            Text(
                                "No hay diálogos asociados.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            ui.dialogs.forEach { dialog ->
                                Card(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            if (dialog.id != null) {
                                                navigator.push(
                                                    DialogDetails(dialog.id)
                                                )
                                            } else {
                                                // Manejo de error si dialog.id es null
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            dialog.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Ver detalles",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                    } ?: run {
                        if (ui.isLoading) {
                            CircularProgressIndicator()
                        } else if (ui.error != null) {
                            Text("Error: ${ui.error}", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    println("Editing level: $editLevel")
                    editLevel?.let { it1 ->
                        EditLevelDetails(
                            initial = it1,
                            onDismiss = { editLevel = null },
                            onSave = { editedLevel ->
                                vm.saveEdits(editedLevel, insertBeforeId, insertAfterId)
                                editLevel = null

                            }
                        )
                    }
                    confirmDelete?.let {
                        AlertDialog(
                            onDismissRequest = { confirmDelete = null },
                            title = { Text("Eliminar nivel") },
                            text = { Text("¿Seguro de eliminar ${it.name}?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    it.id?.let { it1 -> vm.delete(it1) }
                                    confirmDelete = null
                                    navigator.pop()

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLevelDetails(initial: Level, onSave: (Level) -> Unit, onDismiss: () -> Unit) {
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
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DifficultyLevel.entries.forEach { level ->
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
