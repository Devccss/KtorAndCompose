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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
        val error by remember { mutableStateOf<String?>(null) }
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
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                ) {
                    Text("Detalles del Nivel", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.padding(8.dp))
                    ui.fullDialog?.let { dlg ->
                        Card(
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {

                            Column(modifier = Modifier.padding(16.dp)) {
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
                        // Sección Participantes
                        Text("Participantes", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(4.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ui.fullDialog?.participants?.forEach { dto ->

                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(dto.participant.name)
                                    Row {
                                        IconButton({ showEditParticipant = dto.participant }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar participante"
                                            )
                                        }
                                        IconButton({ showDeleteParticipant = dto.participant }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar participante"
                                            )
                                        }
                                    }
                                }
                            }
                            if ((ui.fullDialog?.participants?.size ?: 0) < 5) {
                                Button(onClick = { showAddParticipant = true }) {
                                    Text("Agregar participante")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(8.dp))

                        // Sección Frases
                        Text("Frases", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(4.dp))
                        ui.phrases.forEach { phrase ->
                            val participant = ui.participants.find { it.id == phrase.participantId }
                            val isSaved = phrase.spanishText?.isNotEmpty() == true
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(if (isSaved) Color(0xFFDFFFD6) else Color(0xFFFFF7D6))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Participante: ${participant?.name ?: "?"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Frase: ${phrase.englishText}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (!isSaved) {
                                            Text(
                                                "Traducción pendiente",
                                                color = Color(0xFFFFA000),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        } else {
                                            Text(
                                                "Traducción: ${phrase.spanishText?.joinToString()}",
                                                color = Color(0xFF388E3C),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    IconButton({ showEditPhrase = phrase }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar frase"
                                        )
                                    }
                                    IconButton({ showDeletePhrase = phrase }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar frase"
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(8.dp))

                        // Sección Agregar Frase
                        Text("Agregar Frase", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = phraseInput,
                                onValueChange = { phraseInput = it },
                                label = { Text("Frase en inglés") },
                                modifier = Modifier.weight(2f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Select participante
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { expanded = true },
                                    enabled = ui.participants.isNotEmpty(),
                                ) {
                                    Text(
                                        ui.participants.find { it.id == selectedParticipantId }?.name
                                            ?: "Selecciona participante"
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    ui.participants.forEach { participant ->
                                        DropdownMenuItem(
                                            text = { Text(participant.name) },
                                            onClick = {
                                                selectedParticipantId = participant.id
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        OutlinedTextField(
                            value = phraseSpanishInput,
                            onValueChange = { phraseSpanishInput = it },
                            label = { Text("Traducción (Español)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Button(
                            onClick = {
                                if (phraseInput.isNotBlank() && selectedParticipantId != null) {
                                    val phraseDto = CreatePhraseDto(
                                        audioUrl = null,
                                        englishText = phraseInput,
                                        spanishText = if (phraseSpanishInput.isNotBlank()) listOf(
                                            phraseSpanishInput
                                        ) else emptyList()
                                    )
                                    // Guardar frase solo si tiene traducción
                                    if (phraseSpanishInput.isNotBlank()) {
                                        selectedParticipantId?.let {
                                            vm.createPhrase(it, phraseDto)
                                        }
                                        phraseOrder += 1
                                        phraseInput = ""
                                        phraseSpanishInput = ""
                                        pendingWords = emptyList()
                                        pendingPhrase = null
                                    } else {
                                        pendingPhrase = PhraseDto(
                                            id = -1,
                                            participantId = selectedParticipantId!!,
                                            audioUrl = null,
                                            englishText = phraseInput,
                                            spanishText = emptyList(),
                                            isActive = true,
                                            createdAt = null
                                        )
                                        // Separar palabras
                                        pendingWords = phraseInput.split(" ")
                                            .filter { it.isNotBlank() }
                                            .mapIndexed { _, word ->
                                                WordDto(
                                                    id = -1,
                                                    english = word,
                                                    spanish = "",
                                                    phonetic = null,
                                                    description = null,
                                                    isActive = false
                                                )
                                            }
                                    }
                                }
                            },
                            enabled = phraseInput.isNotBlank() && selectedParticipantId != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar frase")
                        }
                        Spacer(modifier = Modifier.padding(4.dp))

                        // Mostrar frase y palabras pendientes (amarillo)
                        pendingPhrase?.let { phrase ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF7D6))
                                    .padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        "Frase pendiente: ${phrase.englishText}",
                                        color = Color(0xFFFFA000)
                                    )
                                    Text("Participante: ${ui.participants.find { it.id == phrase.participantId }?.name ?: "?"}")
                                    Text("Traducción pendiente")
                                }
                            }
                        }
                        if (pendingWords.isNotEmpty()) {
                            Text(
                                "Palabras pendientes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFA000)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                pendingWords.forEach { word ->
                                    Card(
                                        modifier = Modifier
                                            .background(Color(0xFFFFF7D6))
                                            .padding(2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(4.dp)) {
                                            Text(word.english)
                                            Text(
                                                "Traducción pendiente",
                                                color = Color(0xFFFFA000),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sección Nivel asociado
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("Nivel asociado", style = MaterialTheme.typography.titleMedium)
                        // ...existing code for "Nivel asociado"...

                    } ?: run {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else if (error != null) {
                            Text("Error: $error", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Column {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Nivel asociado", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(4.dp))

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
                                    ui.fullDialog?.dialog?.id?.let{
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

            // Diálogos para agregar/editar/eliminar participantes
            if (showAddParticipant) {
                AddEditParticipantDialog(
                    onSave = { dto ->
                        if (dialogId != null) {
                            vm.createParticipant(
                                dialogId,
                                CreateParticipantDTO(
                                    name = dto.name
                                )
                            )
                        }
                        showAddParticipant = false
                    },
                    onDismiss = { showAddParticipant = false }
                )
            }
            showEditParticipant?.let { participant ->
                AddEditParticipantDialog(
                    initial = participant,
                    onSave = { updatedParticipant ->
                        vm.updateParticipant(
                            updatedParticipant.id,
                            CreateParticipantDTO(
                                name = updatedParticipant.name,
                            )
                        )
                        showEditParticipant = null
                    },
                    onDismiss = { showEditParticipant = null }
                )
            }
            showDeleteParticipant?.let { participant ->
                AlertDialog(
                    onDismissRequest = { showDeleteParticipant = null },
                    title = { Text("Eliminar participante") },
                    text = { Text("¿Seguro de eliminar este participante?") },
                    confirmButton = {
                        TextButton(onClick = {
                            participant.let { vm.deleteParticipant(it.id) }
                            showDeleteParticipant = null
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteParticipant = null }) { Text("Cancelar") }
                    }
                )
            }

            // Diálogos para editar/eliminar frases
            showEditPhrase?.let { phrase ->
                EditPhraseDialog(
                    initial = phrase,
                    onSave = { dto ->
                        phrase.id.let { vm.updatePhrase(it, dto) }
                        showEditPhrase = null
                    },
                    onDismiss = { showEditPhrase = null }
                )
            }
            showDeletePhrase?.let { phrase ->
                AlertDialog(
                    onDismissRequest = { showDeletePhrase = null },
                    title = { Text("Eliminar frase") },
                    text = { Text("¿Seguro de eliminar esta frase?") },
                    confirmButton = {
                        TextButton(onClick = {
                            phrase.id.let { vm.deletePhrase(it) }
                            showDeletePhrase = null
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeletePhrase = null }) { Text("Cancelar") }
                    }
                )
            }

        }
    }
}

// Diálogo para agregar/editar participante
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
                    if (name.isNotBlank() && id != null) {
                        onSave(
                            DialogParticipantDTO(
                                id = id,
                                name = name,
                                dialogId = initial.dialogId,
                                createdAt = initial.createdAt
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Diálogo para editar frase
@Composable
fun EditPhraseDialog(
    initial: PhraseDto,
    onSave: (CreatePhraseDto) -> Unit,
    onDismiss: () -> Unit
) {
    var englishText by remember { mutableStateOf(initial.englishText) }
    var spanishText by remember { mutableStateOf(initial.spanishText?.joinToString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar frase") },
        text = {
            Column {
                OutlinedTextField(
                    value = englishText,
                    onValueChange = { englishText = it },
                    label = { Text("Frase en inglés") }
                )
                OutlinedTextField(
                    value = spanishText,
                    onValueChange = { spanishText = it },
                    label = { Text("Traducción (Español)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (englishText.isNotBlank() && spanishText.isNotBlank()) {
                        onSave(
                            CreatePhraseDto(
                                audioUrl = initial.audioUrl,
                                englishText = englishText,
                                spanishText = spanishText.split(",").map { it.trim() }
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}