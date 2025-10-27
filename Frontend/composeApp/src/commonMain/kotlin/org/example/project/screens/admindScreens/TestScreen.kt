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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateTest
import org.example.project.models.Level
import org.example.project.models.Test
import org.example.project.models.TestType

import org.example.project.viewModel.TestViewModel


class TestScreen:Screen {

    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            TestViewModel(RepositoryProvider.testRepository)
        }

        val ui by vm.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow

        var editing by remember { mutableStateOf<Test?>(null) }
        var confirmDelete by remember { mutableStateOf<Test?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        LaunchedEffect(Unit) {
            vm.refresh()
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
                        currentPage = "Tests",
                        titlePage = "Gestion de Tests",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )

                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Test")
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ){padding ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    when {
                        ui.isLoading -> CircularProgressIndicator()
                        ui.error != null -> Text("Error: ${ui.error}")
                        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                            if (ui.tests.isEmpty()) {
                                item {
                                    Text("No hay tests disponibles.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            items(ui.tests) { test ->
                                TestCard(
                                    test = test,
                                    onClick = {
                                        if (test.id != null) {
                                            navigator.push(
                                                TestDetailsScreen(testId = test.id)
                                            )
                                        } else {
                                            ui.error = "ID de Test no disponible"
                                        }

                                    },
                                    onEdit = { editing = test },
                                    levels = ui.levels,
                                    onDelete = { confirmDelete = test }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }


                editing?.let { test ->
                    EditTestDialog(
                        initial = test,
                        onSave = { updated ->
                            updated.id?.let { id ->
                                vm.editTest(CreateTest(
                                    name = updated.name,
                                    description = updated.description,
                                    testType = updated.testType
                                ) ,id)
                            }
                            editing = null
                        },
                        levels = ui.levels,
                        onDismiss = { editing = null }
                    )
                }


                if (showAddDialog) {
                    EditTestDialog(
                        initial = Test(
                            name = "",
                            description = "",
                            testType = TestType.TRANSLATION,
                            id = 0,
                            isActive = false,
                            levelId = 0
                        ),
                        levels = ui.levels,
                        onSave = { newTest ->
                            vm.createTest(CreateTest(
                                name = newTest.name,
                                description = newTest.description,
                                testType = newTest.testType,
                                isActive = newTest.isActive
                            ), newTest.levelId)

                            showAddDialog = false

                        },
                        onDismiss = { showAddDialog = false }
                    )
                }

                // Eliminar diálogo
                confirmDelete?.let { test ->
                    AlertDialog(
                        onDismissRequest = { confirmDelete = null },
                        title = { Text("Eliminar Test") },
                        text = { Text("¿Seguro de eliminar este Test?") },
                        confirmButton = {
                            TextButton(onClick = {
                                test.id?.let { vm.deleteTest(it) }
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
fun TestCard(
    test: Test,
    levels: List<Level>,
    onClick: () -> Unit = {},
    onEdit: (Test) -> Unit,
    onDelete: (Test) -> Unit
) {
    val levelName = levels.find { it.id == test.levelId }?.name ?: "Desconocido"

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
                    Text(test.id?.toString() ?: "N", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Row {
                        Text(
                            test.name,
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
                                text = (if (test.isActive == true) "Activo" else "Inactivo"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (test.isActive == true) Color(
                                    34,
                                    139,
                                    34
                                ) else Color(178, 34, 34),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                }
                IconButton(
                    onClick = { onEdit(test) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                Spacer( Modifier.width(8.dp))
                IconButton(
                    onClick = { onDelete(test) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Nivel: $levelName", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text("Descripción: ${test.description}", style = MaterialTheme.typography.bodySmall)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTestDialog(
    initial: Test,
    levels: List<Level>,
    onSave: (Test) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var levelId by remember { mutableStateOf(initial.levelId) }
    var testTypeSelected by remember { mutableStateOf(initial.testType) }
    var expanded by remember { mutableStateOf(false) }
    var activeExpanded by remember { mutableStateOf(false) }
    var activeTest by remember { mutableStateOf(initial.isActive ?: false) }
    var typeExpanded by remember { mutableStateOf(false) }
    val selectedLevel = levels.find { it.id == levelId }

    val testTypes = TestType.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Test" else "Editar Test") },
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
                ExposedDropdownMenuBox(
                    expanded = activeExpanded,
                    onExpandedChange = { activeExpanded = !activeExpanded }
                ) {
                    OutlinedTextField(
                        value = activeTest.let { if (it) "Activo" else "Inactivo" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activeExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = activeExpanded,
                        onDismissRequest = { activeExpanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Activo") },
                            onClick = {
                                activeTest = true
                                activeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Inactivo") },
                            onClick = {
                                activeTest = false
                                activeExpanded = false
                            }
                        )

                    }
                }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = testTypeSelected?.name ?: "Selecciona un tipo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        testTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    testTypeSelected = type
                                    typeExpanded = false
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
                            description = description,
                            isActive = activeTest,
                            levelId = levelId,
                            testType = testTypeSelected
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