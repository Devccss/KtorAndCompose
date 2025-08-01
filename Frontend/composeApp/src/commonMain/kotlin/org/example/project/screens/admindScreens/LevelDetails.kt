package org.example.project.screens.admindScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.models.Dialog
import org.example.project.models.Level
import org.example.project.viewModel.LevelsViewModel


class LevelDetails(
    private val levelId: Int?,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            LevelsViewModel(RepositoryProvider.levelRepository)
        }
        var level by remember { mutableStateOf<Level?>(null) }
        var dialogs by remember { mutableStateOf<List<Dialog>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var editing by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(navigator.lastItem,editing,confirmDelete ) {
            if (levelId != null) {
                try {
                    isLoading = true
                    level = RepositoryProvider.levelRepository.getLevelById(levelId)
                    dialogs = RepositoryProvider.dialogsRepository.getDialogsByLevelId(levelId)
                    error = null
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        }
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navigator.push(route)
                    }
                )
            }){
            Scaffold(
                topBar = {
                    AdminTopBar(
                        currentPage = "LevelDetails",
                        titlePage = "Detalles del Nivel",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                },
            ){
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                ) {
                    Text("Detalles del Nivel", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.padding(8.dp))

                    level?.let { lvl ->
                        androidx.compose.material3.Card(
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Nombre: ${lvl.name}", style = MaterialTheme.typography.titleMedium)
                                Text("Descripción: ${lvl.description}", style = MaterialTheme.typography.bodyMedium)
                                Text("Dificultad: ${lvl.difficulty}", style = MaterialTheme.typography.bodySmall)
                                Text("Orden: ${lvl.orderLevel}", style = MaterialTheme.typography.bodySmall)
                                Row {
                                    Row {
                                        IconButton({ editing = true }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        }
                                        IconButton({ confirmDelete = true }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }

                        // Sección de diálogos asociados
                        Text("Diálogos asociados", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(4.dp))
                        if (dialogs.isEmpty()) {
                            Text("No hay diálogos asociados.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            dialogs.forEach { dialog ->
                                androidx.compose.material3.Card(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            navigator.push(DialogDetails(dialog.id))
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(dialog.name, style = MaterialTheme.typography.bodyMedium)
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
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else if (error != null) {
                            Text("Error: $error", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    if (editing){
                        level?.let {
                            EditLevelDialog(
                                initial = it,
                                onSave = { editedLevel ->
                                    vm.saveEdits(editedLevel, null, null)
                                    editing = false

                                },
                                onDismiss = { editing = false },
                            )
                        }
                    }
                }
            }
        }
    }
}