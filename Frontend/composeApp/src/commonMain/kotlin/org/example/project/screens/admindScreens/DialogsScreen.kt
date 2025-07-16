package org.example.project.screens.admindScreens

import RepositoryProvider
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import org.example.project.models.Dialog
import org.example.project.models.DifficultyLevel
import org.example.project.viewModel.DialogViewModel

class DialogsScreen(private val levelId: Int?) : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            DialogViewModel(RepositoryProvider.dialogsRepository)
        }
        val ui by vm.state.collectAsState()
        var editing by remember { mutableStateOf<Dialog?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf<Dialog?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Administrar Diálogos") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar diálogo")
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                when {
                    ui.isLoading -> CircularProgressIndicator()
                    ui.error != null -> Text("Error: ${ui.error}")
                    else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                        items(ui.dialogs) { dialog ->
                            DialogCard(
                                dialog = dialog,
                                onEdit = { editing = it },
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
                    onSave = { newDialog ->
                        if (levelId != null) {
                            vm.addDialog(
                                newDialog,
                                levelId
                            )
                        }
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

@Composable
fun DialogCard(dialog: Dialog, onEdit: (Dialog) -> Unit, onDelete: (Dialog) -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Nombre: ${dialog.name}", style = MaterialTheme.typography.titleMedium)
            Text("Descripción: ${dialog.description}", style = MaterialTheme.typography.bodySmall)
            Row {
                IconButton(onClick = { onEdit(dialog) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onDelete(dialog) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialogDialog(initial: Dialog, onSave: (Dialog) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Diálogo" else "Editar Diálogo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(initial.copy(name = name, description = description))
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
