package org.example.project.screens.admindScreens

import RepositoryProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTest
import org.example.project.dtos.DialogTestDto
import org.example.project.models.Dialog
import org.example.project.models.Test
import org.example.project.viewModel.TestDetailsViewModel

class TestDetailsScreen(private val testId: Int) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            TestDetailsViewModel(
                testId = testId,
                testRepository = RepositoryProvider.testRepository
            )
        }
        val ui by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

        var editingTest by remember { mutableStateOf<Test?>(null) }
        var confirmDelete by remember { mutableStateOf<Test?>(null) }
        var confirmDeleteTestDialog by remember { mutableStateOf<Dialog?>(null) }
        var showAssignDialog by remember { mutableStateOf(false) }


        LaunchedEffect(ui.error) {
            ui.error?.let { snackbarHostState.showSnackbar(it) }
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
                        currentPage = "TestDetails",
                        titlePage = "Detalles del Test",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                },

                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text("Detalles del Test", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    ui.test?.let { test ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Nombre: ${test.name}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Card(
                                            modifier = Modifier
                                                .padding(end = 4.dp, bottom = 4.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Text(
                                                text = if (test.isActive == true) "Activo" else "Inactivo",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (test.isActive == true) Color(0xFF228B22) else Color(
                                                    0xFFB22222
                                                ),
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Descripción: ${test.description}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Tipo: ${test.testType ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Nivel asociado: ${ui.level?.name ?: "Desconocido"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { editingTest = test },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Test")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        onClick = { confirmDelete = test },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar Test"
                                        )
                                    }
                                }
                            }
                        }

                        // Diálogos asociados
                        Row {
                            Text("Diálogos asociados", style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = { showAssignDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Black
                                )
                            ){
                                Text("Asignar dialogo",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.Underline
                                    ))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (ui.assignDialogs.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "No hay diálogos asociados",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            ui.assignDialogs.forEach { dlg ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)

                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(dlg.name, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = {
                                                dlg.id?.let { navigator.push(DialogDetails(it)) }
                                            }

                                        ){
                                            Icon(
                                                Icons.AutoMirrored.Filled.List,
                                                contentDescription = "Ver Detalles del Diálogo"
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                dlg.id?.let { confirmDeleteTestDialog = dlg }
                                            }

                                        ){
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Ver Detalles del Diálogo"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } ?: run {
                        if (ui.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        } else {
                            ui.error?.let {
                                Text(
                                    "Error: No se enocontró el test.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Edit dialog
                editingTest?.let { initial ->
                    EditTestDialog(
                        initial = initial,
                        levels = ui.levels,
                        onSave = { edited ->
                            edited.id?.let {
                                vm.editTest(
                                    CreateTest(
                                        name = edited.name,
                                        description = edited.description,
                                        testType = edited.testType,
                                        isActive = edited.isActive
                                    )
                                )
                            }
                            editingTest = null
                        },
                        onDismiss = { editingTest = null }
                    )
                }

                if(showAssignDialog) {
                    assignDialogToTest(
                        testId = testId,
                        dialogs = ui.dialogs.filterNot { d -> ui.assignDialogs.any { ad -> ad.id == d.id } },
                        onSave = { dialog ->
                            if (dialog.id != null) {
                                vm.assignDialogToTest(dialog.id)
                            }else{
                                ui.error = "Error al asignar el diálogo"
                            }
                            showAssignDialog = false
                        },
                        onDismiss = { showAssignDialog = false }
                    )
                }

                // Confirm delete
                confirmDelete?.let { t ->
                    AlertDialog(
                        onDismissRequest = { confirmDelete = null },
                        title = { Text("Eliminar Test") },
                        text = { Text("¿Seguro que deseas eliminar '${t.name}'?") },
                        confirmButton = {
                            TextButton(onClick = {
                                t.id?.let { vm.deleteTestDetails(it) }
                                confirmDelete = null
                            }) { Text("Eliminar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
                        }
                    )
                }
                confirmDeleteTestDialog?.let { d ->
                    AlertDialog(
                        onDismissRequest = { confirmDeleteTestDialog = null },
                        title = { Text("Eliminar Test") },
                        text = { Text("¿Seguro que deseas eliminar '${d.name}' del test ${ui.test?.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                d.id?.let { vm.deleteDialogFromTest(it,testId) }
                                confirmDeleteTestDialog = null
                            }) { Text("Eliminar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmDeleteTestDialog = null }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun assignDialogToTest(
    testId: Int,
    dialogs: List<Dialog>,
    onSave: (Dialog)->Unit,
    onDismiss: () -> Unit
) {
    var selectDialog by remember { mutableStateOf<Dialog?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var dialogId by remember { mutableStateOf<Int?>(null) }
    var selectedIds = remember { mutableStateListOf<Int>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Diálogo al Test") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selecciona uno o más diálogos:")
                Column {
                    dialogs.forEach { dialog ->
                        val id = dialog.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (id != null) {
                                        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = id != null && selectedIds.contains(id),
                                onCheckedChange = { checked ->
                                    if (id != null) {
                                        if (checked) selectedIds.add(id) else selectedIds.remove(id)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dialog.name, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {

                dialogs.filter { d -> d.id != null && selectedIds.contains(d.id) }
                    .forEach { selected -> onSave(selected) }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = {onDismiss}) { Text("Cancelar") }
        }
    )
}
