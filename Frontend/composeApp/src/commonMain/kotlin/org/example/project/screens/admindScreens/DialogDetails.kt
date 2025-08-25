package org.example.project.screens.admindScreens

import RepositoryProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.example.project.dtos.CreateParticipantDTO
import org.example.project.dtos.CreatePhraseDto
import org.example.project.dtos.PhraseDto
import org.example.project.dtos.WordDto
import org.example.project.dtos.DialogParticipantDTO

import org.example.project.viewModel.DialogDetailsViewModel


class DialogDetails(
    private val dialogId: Int,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = rememberScreenModel {
            DialogDetailsViewModel(
                dialogId,
                RepositoryProvider.dialogsRepository,
                RepositoryProvider.participantsRepository,
                RepositoryProvider.phrasesRepository,
                RepositoryProvider.wordsRepository,
                RepositoryProvider.phraseWordRepository
            )
        }
        val ui by vm.state.collectAsState()

        val isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var editing by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf(false) }

        // Estados para secciones
        var showAddParticipant by remember { mutableStateOf(false) }
        var showEditParticipant by remember { mutableStateOf<DialogParticipantDTO?>(null) }
        var showDeleteParticipant by remember { mutableStateOf<DialogParticipantDTO?>(null) }
        var showEditPhrase by remember { mutableStateOf<PhraseDto?>(null) }
        var showDeletePhrase by remember { mutableStateOf<PhraseDto?>(null) }
        var selectedParticipantId by remember { mutableStateOf<Int?>(null) }
        var phraseInput by remember { mutableStateOf("") }
        var phraseSpanishInput by remember { mutableStateOf("") }
        var phraseOrder by remember { mutableStateOf(1) }
        var pendingWords by remember { mutableStateOf<List<WordDto>>(emptyList()) }
        var pendingPhrase by remember { mutableStateOf<PhraseDto?>(null) }
        var showAddPhraseSection by remember { mutableStateOf(false) }
        var showHelpAddPhrase by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val navigator = LocalNavigator.currentOrThrow

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
                        currentPage = "DialogDetails",
                        titlePage = "Detalles del Dialogo",
                        onBack = { navigator.pop() },
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                },
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ui.fullDialog?.let { dlg ->
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
                                    Text(
                                        "Nombre: ${dlg.dialog.name}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Descripción: ${dlg.dialog.description}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Dificultad: ${dlg.dialog.difficulty}",
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
                                    IconButton({ editing = true }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(777899) // Azul suave
                                        )
                                    }
                                    IconButton({ confirmDelete = true }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(777899) // Rojo suave
                                        )
                                    }
                                }
                            }
                        }
                        // Sección Participantes
                        Text("Participantes", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        if ((ui.fullDialog?.participants?.size ?: 0) < 5) {
                            Button(onClick = { showAddParticipant = true }) {
                                Text("Agregar participante")
                            }
                        }
                        ui.participants.forEach { dto ->
                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(dto.name)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton({ showEditParticipant = dto }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar participante"
                                            )
                                        }
                                        IconButton({ showDeleteParticipant = dto }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar participante"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Sección frases
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Frases", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(0.5.dp))
                            IconButton(onClick = { showHelpAddPhrase = true } ) {
                                Icon(
                                    modifier = Modifier.weight(0.1f),
                                    imageVector = Icons.AutoMirrored.Filled.Help,
                                    contentDescription = "Ayuda para agregar frase"
                                )
                            }
                        }
                        if (showHelpAddPhrase) {
                            AlertDialog(
                                onDismissRequest = { showHelpAddPhrase = false },
                                title = { Text("Ayuda para agregar frase") },
                                text = { Text("Escribe una frase y automáticamente se tomará cada palabra de la misma para traducirlas luego.") },
                                confirmButton = {
                                    TextButton(onClick = { showHelpAddPhrase = false }) {
                                        Text("Entendido")
                                    }
                                }
                            )
                        }
                        if(ui.phrases.isEmpty()) {
                            Button(onClick = { showAddPhraseSection = true }) {
                                Text("Agregar frase")

                            }
                        }else{
                            Button(onClick = { showAddPhraseSection = true }) {
                                Text("+")
                            }
                            ui.phrases.forEach { phrase ->
                                Card(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {

                                            Text("Frase: ${phrase.englishText}")
                                            Text("Traducción: ${phrase.spanishText}")
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { showEditPhrase = phrase }
                                            ) {
                                                Text("...")
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        // Sección Nivel asociado
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nivel asociado", style = MaterialTheme.typography.titleMedium)
                         ui.level?.let{ level ->
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        if (level.id != null) {
                                            navigator.push(
                                                LevelDetails(level.id)
                                            )
                                        }else {
                                            error = "El diálogo no tiene ID"
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(level.name, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Ver detalles",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
                }
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nivel asociado", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                            .clickable { navigator.push(LevelDetails(ui.level?.id)) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            ui.level?.let { it1 ->
                                Text(
                                    it1.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Ver detalles",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (editing) {
                        ui.fullDialog?.dialog?.let { it1 ->
                            EditDialogDialog(
                                initial = it1,
                                levels = ui.levels,
                                onSave = { update ->
                                    update.id?.let { vm.updateDialog(it, update) }
                                    editing = false
                                },
                                onDismiss = { editing = false }
                            )
                        }
                    }
                    if (confirmDelete) {
                        AlertDialog(
                            onDismissRequest = { confirmDelete = false },
                            title = { Text("Eliminar diálogo") },
                            text = { Text("¿Seguro de eliminar este diálogo?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    ui.fullDialog?.dialog?.id?.let {
                                        vm.deleteDialog(it)
                                        navigator.pop()
                                    }
                                    confirmDelete = false
                                }) { Text("Eliminar") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    confirmDelete = false
                                }) { Text("Cancelar") }
                            }
                        )
                    }
                }
            }


            showDeleteParticipant?.let { participant ->
                AlertDialog(
                    onDismissRequest = { showDeleteParticipant = null },
                    title = { Text("Eliminar participante") },
                    text = { Text("¿Seguro de eliminar este participante?") },
                    confirmButton = {
                        TextButton(onClick = {
                            participant.id?.let { vm.deleteParticipant(it) }
                            showDeleteParticipant = null
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteParticipant = null }) { Text("Cancelar") }
                    }
                )
            }


            // Diálogo para agregar y editar participantes
            showAddParticipant.takeIf { it }?.let {
                AddEditParticipantDialog(
                    initial = null,
                    onSave = { newParticipant ->
                        vm.createParticipant(
                            dialogId,
                            participant = CreateParticipantDTO(
                                name = newParticipant.name
                            )
                        )
                        showAddParticipant = false
                    },
                    onDismiss = { showAddParticipant = false }
                )
            }
            showEditParticipant?.let { participant ->
                AddEditParticipantDialog(
                    initial = DialogParticipantDTO(
                        id = participant.id,
                        dialogId = participant.dialogId,
                        name = participant.name,
                        createdAt = participant.createdAt
                    ),
                    onSave = { dto ->
                        dto.id?.let {
                            vm.updateParticipant(
                                it,
                                participant = CreateParticipantDTO(name = dto.name)
                            )
                        }
                        showEditParticipant = null
                    },
                    onDismiss = { showEditParticipant = null }
                )
            }
        }
    }
}



@Composable
fun AddEditParticipantDialog(
    initial: DialogParticipantDTO? = null,
    onSave: (DialogParticipantDTO) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    val id = initial?.id

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Agregar participante" else "Editar participante") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        DialogParticipantDTO(
                            id = id,
                            name = name,
                            dialogId = initial?.dialogId,
                            createdAt = initial?.createdAt
                        )
                    )
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}