package org.example.project.screens.admindScreens

import RepositoryProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.example.project.dtos.CreateParticipantDTO
import org.example.project.dtos.CreatePhraseDto
import org.example.project.dtos.DialogParticipantDTO
import org.example.project.dtos.PhraseDto
import org.example.project.dtos.WordDto
import org.example.project.models.Phrase
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
        val selectedParticipantId by remember { mutableStateOf<Int?>(null) }
        val phraseInput by remember { mutableStateOf("") }
        val phraseSpanishInput by remember { mutableStateOf<String>("") }
        var phraseOrder by remember { mutableStateOf(1) }
        var pendingWords by remember { mutableStateOf<List<WordDto>>(emptyList()) }
        var pendingPhrase by remember { mutableStateOf<PhraseDto?>(null) }
        var showAddPhraseSection by remember { mutableStateOf(false) }
        var showEditPhraseSection by remember { mutableStateOf(false) }
        var showHelpAddPhrase by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(ui.error) {
            ui.error?.let {
                snackbarHostState.showSnackbar(it)
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
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(it)
                        .padding(horizontal = 16.dp)
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                ) {
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
                                    Row {
                                        Text(
                                            "Nombre: ${dlg.dialog.name}",
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
                                                text = (if (dlg.dialog.isActive == true) "Activo" else "Inactivo"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (dlg.dialog.isActive == true) Color(
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
                                    IconButton({ editing = true },
                                        modifier = Modifier.size(18.dp)) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",

                                            )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton({ confirmDelete = true },
                                        modifier = Modifier.size(18.dp)) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",

                                            )
                                    }
                                }
                            }
                        }
                        // Sección Participantes
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Participantes",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.weight(0.1f))
                            if ((ui.participants.size) < 5) {
                                TextButton(
                                    onClick = { showAddParticipant = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        "Agregar participante",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                }
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
                                        IconButton(
                                            { showEditParticipant = dto },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar participante"
                                            )
                                        }
                                        Spacer( modifier = Modifier.width(6.dp))
                                        IconButton(
                                            { showDeleteParticipant = dto },
                                            modifier = Modifier.size(18.dp)
                                        ) {
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
                            IconButton(onClick = { showHelpAddPhrase = true }) {
                                Icon(
                                    modifier = Modifier.weight(0.1f),
                                    imageVector = Icons.AutoMirrored.Filled.Help,
                                    contentDescription = "Ayuda para agregar frase"
                                )
                            }
                            if (ui.phrases.isEmpty()) {
                                TextButton(
                                    onClick = { showAddPhraseSection = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        "Agregar Frase",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = { showAddPhraseSection = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        "Agregar Frase",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                }
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

                        ui.phrases.forEach { phrase ->
                            val phraseParticipant = ui.participants.find { participant ->
                                phrase.participantId == participant.id
                            }
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

                                        Text("${phraseParticipant?.name ?: "Participante desconocido"}: ${phrase.englishText}")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Traducciones: ",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        FlowRow(
                                            modifier = Modifier.padding(top = 4.dp),
                                        ) {

                                            phrase.spanishText?.forEach { spanishPhrase ->

                                                Card(
                                                    modifier = Modifier
                                                        .padding(end = 4.dp, bottom = 4.dp)
                                                        .background(Color(220, 220, 220)),
                                                    shape = MaterialTheme.shapes.small,
                                                    elevation = CardDefaults.cardElevation(
                                                        defaultElevation = 2.dp
                                                    )
                                                ) {
                                                    Text(
                                                        text = spanishPhrase,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier
                                                            .padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                showEditPhrase = phrase
                                                showEditPhraseSection = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = Color.Black
                                            )
                                        ) {
                                            Text(
                                                "editar",
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        // Sección Nivel asociado
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nivel asociado", style = MaterialTheme.typography.titleMedium)
                        ui.level?.let { level ->
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        if (level.id != null) {
                                            navigator.push(
                                                LevelDetails(level.id)
                                            )
                                        } else {
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
                                        tint = Color.Black
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
                                participantId = it,
                                participant = CreateParticipantDTO(name = dto.name)
                            )
                        }
                        showEditParticipant = null
                    },
                    onDismiss = { showEditParticipant = null }
                )
            }

            showAddPhraseSection.takeIf { it }?.let {
                AddPhraseSection(
                    phrase = Phrase(
                        id = null,
                        participantId = selectedParticipantId ?: 0,
                        audioUrl = null,
                        isActive = true,
                        createdAt = null
                    ),
                    participants = ui.participants,
                    onDismiss = { showAddPhraseSection = false },
                    onSave = { phraseDto ->
                        vm.createPhrase(
                            participantId = phraseDto.participantId,
                            phrase = CreatePhraseDto(
                                participantId = phraseDto.participantId,
                                englishText = phraseDto.englishText,
                                spanishText = phraseDto.spanishText,
                                audioUrl = null,
                            )
                        )
                        showAddPhraseSection = false
                    }
                )
            }
            showEditPhraseSection.takeIf { it }?.let {
                showEditPhrase?.let { phrase ->
                    AddPhraseSection(
                        phrase = Phrase(
                            id = phrase.id,
                            participantId = phrase.participantId,
                            audioUrl = phrase.audioUrl,
                            englishText = phrase.englishText,
                            spanishText = phrase.spanishText,
                            isActive = phrase.isActive,
                            createdAt = phrase.createdAt
                        ),
                        participants = ui.participants,
                        onDismiss = { showEditPhraseSection = false },
                        onSave = { phraseDto ->
                            phrase.id.let {
                                vm.updatePhrase(
                                    phraseId = it,
                                    phrase = CreatePhraseDto(
                                        participantId = phraseDto.participantId,
                                        englishText = phraseDto.englishText,
                                        spanishText = phraseDto.spanishText,
                                        audioUrl = null,
                                    )
                                )
                            }
                            showEditPhraseSection = false
                        },
                        isEdit = true
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddPhraseSection(
        phrase: Phrase,
        participants: List<DialogParticipantDTO>,
        onDismiss: () -> Unit,
        onSave: (CreatePhraseDto) -> Unit,
        isEdit: Boolean = false
    ) {
        var englishText by remember { mutableStateOf(phrase.englishText ?: "") }
        var phraseSpanish by remember { mutableStateOf("") }
        var listSpanish by remember {
            mutableStateOf<List<String>>(
                phrase.spanishText ?: emptyList()
            )
        }

        var participantId by remember { mutableStateOf<Int?>(phrase.participantId) }
        var expanded by remember { mutableStateOf(false) }

        // Separar palabras en tiempo real
        val words = remember(englishText) { englishText.split(" ").filter { it.isNotBlank() } }
        val participantSelect = participants.find { it.id == participantId }
        var showError by remember { mutableStateOf(false) }
        var errorString by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (isEdit) "Editar frase" else "Agregar frase") },
            text = {
                Column {
                    OutlinedTextField(
                        value = englishText,
                        onValueChange = { newValue ->
                            // Permite letras, espacios y símbolos, pero no números
                            if (newValue.all { !it.isDigit() }) {
                                englishText = newValue
                            }
                        },
                        label = { Text("Frase en inglés") }
                    )

                    if (words.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            words.filter { it.all { char -> char.isLetter() } }.forEach { word ->
                                Card(
                                    modifier = Modifier
                                        .padding(end = 4.dp, bottom = 4.dp)
                                        .background(Color(220, 220, 220)),
                                    shape = MaterialTheme.shapes.small,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = phraseSpanish,
                            onValueChange = { phraseSpanish = it },
                            label = { Text("Traducción/nes al español") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (phraseSpanish.isNotBlank()) {
                                    listSpanish = listSpanish + phraseSpanish
                                    phraseSpanish = ""
                                }
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .height(40.dp)
                                .width(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+")
                        }
                    }
                    Text("Traducciones agregadas:", style = MaterialTheme.typography.bodyMedium)
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        listSpanish.forEach { phrase ->
                            Card(
                                modifier = Modifier
                                    .padding(end = 4.dp, bottom = 4.dp)
                                    .background(Color(220, 220, 220)),
                                shape = MaterialTheme.shapes.small,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = phrase,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = participantSelect?.name ?: "Selecciona un Participante",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Participante") },
                            modifier = Modifier
                                .menuAnchor(
                                    MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            participants.forEach { participant ->
                                DropdownMenuItem(
                                    text = { Text(participant.name) },
                                    onClick = {
                                        participantId = participant.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text("Agregar audio")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(240, 240, 240)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Área para subir o grabar audio (pendiente de implementar)")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (englishText.isBlank()) {
                            showError = true
                        } else if (participantId == null) {
                            showError = true
                        } else {
                            onSave(
                                CreatePhraseDto(
                                    participantId = participantId ?: 0,
                                    englishText = englishText,
                                    spanishText = listSpanish,
                                    audioUrl = null,
                                )
                            )
                        }
                    }
                ) { Text(if (isEdit) "Actualizar" else "Guardar") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }
}