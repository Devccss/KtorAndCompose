package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import org.example.project.models.DifficultyLevel
import org.example.project.viewModel.LevelsViewModel
import org.example.project.models.Level
import org.example.project.network.KtorLevelRepository
import org.example.project.network.createHttpClient


class AdminLevelsScreen : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val vm = rememberScreenModel {
            LevelsViewModel(
                KtorLevelRepository(
                    httpClient = createHttpClient(),
                    baseUrl = "http://10.0.2.2:17986"   // <-- cambia esto
                )
            )
        }

        val ui by vm.state.collectAsState()
        if (ui.error != null) {
            Text("Error: ${ui.error}")
        }

        // ───────────────────────── UI ─────────────────────────
        var editing by remember { mutableStateOf<Level?>(null) }
        var confirmDelete by remember { mutableStateOf<Level?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }
        Scaffold(
            topBar = { TopAppBar(title = { Text("Admin Levels") }) },
            floatingActionButton = {
                Row {
                    FloatingActionButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                    Spacer(Modifier.width(16.dp))
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Text("+")
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), Alignment.Center) {
                when {
                    ui.isLoading -> CircularProgressIndicator()
                    ui.error != null -> Text("Error: ${ui.error}")
                    else -> LevelsList(
                        levels = ui.levels,
                        onEdit = { editing = it },
                        onDelete = { confirmDelete = it }
                    )
                }
            }
        }
        // Diálogo para agregar nivel
        if (showAddDialog) {
            EditLevelDialog(
                initial = Level(id = null, name = "", description = "", order = 0 , accent = 0, difficulty = DifficultyLevel.A1),
                onSave = {
                    vm.addLevel(it)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        /* ——— Diálogo “Editar” ——— */
        editing?.let { level ->
            EditLevelDialog(
                initial = level,
                onSave = { vm.saveEdits(it); editing = null },
                onDismiss = { editing = null }
            )
        }

        /* ——— Confirmación “Eliminar” ——— */
        confirmDelete?.let { level ->
            AlertDialog(
                onDismissRequest = { confirmDelete = null },
                title = { Text("Eliminar nivel") },
                text = { Text("¿Seguro de eliminar «${level.name}»?") },
                confirmButton = {
                    TextButton(onClick = {
                        level.id?.let { vm.delete(it) }
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

/*─────────────────────────────────────────────────────*/
/* LISTA y CARD reutilizadas (idénticas a las tuyas)   */
@Composable
fun LevelsList(
    levels: List<Level>,
    onEdit: (Level) -> Unit,
    onDelete: (Level) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize()) {
        items(levels) { level ->
            LevelCard(
                level = level,
                onEdit = { onEdit(level) },
                onDelete = { onDelete(level) }
            )
            Divider()
        }
    }
}

@Composable
fun LevelCard(level: Level, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${level.order}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(0.1f))
                Text(level.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(0.4f))
                Text(level.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.5f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Eliminar") }
            }
        }
    }
}

/*─────────────────────────────────────────────────────*/
/* Diálogo simple para editar nombre/descr.            */
@Composable
fun EditLevelDialog(
    initial: Level,
    onSave: (Level) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var desc by remember { mutableStateOf(initial.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nivel") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(desc, { desc = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(initial.copy(name = name, description = desc))
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
