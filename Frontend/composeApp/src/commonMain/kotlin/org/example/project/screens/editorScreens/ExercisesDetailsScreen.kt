package org.example.project.screens.editorScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import kotlinx.coroutines.launch
import org.example.project.components.CustomTextField
import org.example.project.components.EditorLayout
import org.example.project.dtos.ContentType
import org.example.project.dtos.CreateAlternativeDto
import org.example.project.dtos.CreateExerciseContentDto
import org.example.project.dtos.CreateQuestionDto
import org.example.project.dtos.CreateWordDto
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateExerciseContentDto
import org.example.project.dtos.UpdateExerciseDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.dtos.UpdateWordDto
import org.example.project.network.RepositoryProvider
import org.example.project.service.AiQuestionGenerationService
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.AiQuestionGenerationViewModel
import org.example.project.viewModel.QuestionViewModel
import org.example.project.viewModel.WordViewModel
import org.jetbrains.compose.resources.Font
import kotlin.random.Random

class DraftAlternative(
    val id: Int? = null,
    text: String,
    isCorrect: Boolean,
    val tempId: Long = Random.nextLong()
) {
    var text by mutableStateOf(text)
    var isCorrect by mutableStateOf(isCorrect)
}

class ContentDraft(
    val id: Int,
    exerciseId: Int,
    contentType: ContentType,
    textContent: String,
    grammarExplanation: String,
    audioUrl: String? = null,
) {
    var exerciseId by mutableStateOf(exerciseId)
    var textContent by mutableStateOf(textContent)
    var grammarExplanation by mutableStateOf(grammarExplanation)
    var contentType by mutableStateOf(contentType)
    var audioUrl by mutableStateOf(audioUrl)
}

class WordDraftState(
    val id: Int,
    english: String,
    spanish: String,
    phonetic: String? = null,
    description: String? = null,
    isActive: Boolean = false,
) {
    var english by mutableStateOf(english)
    var spanish by mutableStateOf(spanish)
    var phonetic by mutableStateOf(phonetic)
    var description by mutableStateOf(description)
    var isActive by mutableStateOf(isActive)
}

class QuestionDraftState(
    val id: Int,
    questionText: String,
    alternatives: List<DraftAlternative>,
    orderQuestion: Int,
    isActive: Boolean = false,
) {
    var questionText by mutableStateOf(questionText)
    var orderQuestion by mutableStateOf(orderQuestion)
    var alternatives = mutableStateMapOf<Long, DraftAlternative>().apply {
        alternatives.forEach { put(it.tempId, it) }
    }
    var isActive by mutableStateOf(isActive)
}

class ExercisesDetailsScreen(private val exerciseId: Int, private val unitId: Int) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val exerciseVm = rememberScreenModel {
            ExercisesViewModel(
                RepositoryProvider.exerciseRepo,
                unitId,
                exerciseId
            )
        }
        val questionVm =
            rememberScreenModel { QuestionViewModel(RepositoryProvider.questionRepo, exerciseId) }
        val aiQuestionVm = rememberScreenModel {
            AiQuestionGenerationViewModel(
                AiQuestionGenerationService(RepositoryProvider.aiQuestionGenerationRepo)
            )
        }
        val wordsVm = rememberScreenModel { WordViewModel(RepositoryProvider.wordRepo) }
        val exerciseUi by exerciseVm.state.collectAsState()
        val questionUi by questionVm.state.collectAsState()
        val aiUi by aiQuestionVm.state.collectAsState()
        val wordsUi by wordsVm.state.collectAsState()

        var selectedIndex by remember { mutableStateOf(3) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
        val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

        var onDeleteExercise by remember { mutableStateOf(false) }
        var onDeleteContent by remember { mutableStateOf(false) }
        val confirmChecked = remember { mutableStateOf(false) }

        //Content
        var isAddingContent by remember { mutableStateOf(false) }
        var contentText by remember { mutableStateOf("") }
        var contentGrammar by remember { mutableStateOf("") }
        var contentUrlAudio by remember { mutableStateOf("") }
        var contentDraft by remember {
            mutableStateOf(
                ContentDraft(
                    id = 0,
                    exerciseId = exerciseId,
                    contentType = ContentType.READING,
                    textContent = "Explicación nula",
                    grammarExplanation = "Explicación gramatical nula",
                    audioUrl = null
                )
            )
        }

        // Words
        var isAddingWord by remember { mutableStateOf(false) }
        var newWordEnglish by remember { mutableStateOf("") }
        var newWordSpanish by remember { mutableStateOf("") }
        var newPhonetic by remember { mutableStateOf("") }
        var newWordDescription by remember { mutableStateOf("") }
        var isActiveWord by remember { mutableStateOf(false) }
        val wordDrafts = remember { mutableStateMapOf<Int, WordDraftState>() }
        val expandedWordCards = remember { mutableStateMapOf<Int, Boolean>() }

        //Question
        var isAddingQuestion by remember { mutableStateOf(false) }
        var isEditingExercise by remember { mutableStateOf(false) }
        var isEditingContent by remember { mutableStateOf(false) }
        var isEditingWords by remember { mutableStateOf(false) }
        var isEditingQuestions by remember { mutableStateOf(false) }
        var questionText by remember { mutableStateOf("") }

        // Estado para nuevas alternativas
        var alternatives by remember { mutableStateOf(listOf<CreateAlternativeDto>()) }
        var newAltText by remember { mutableStateOf("") }

        // --- EXERCISE DRAFTS ---
        var draftName by remember { mutableStateOf("") }
        var draftDescription by remember { mutableStateOf("") }
        var draftActive by remember { mutableStateOf(false) }



        // --- QUESTION DRAFTS ---
        val questionDrafts = remember { mutableStateMapOf<Int, QuestionDraftState>() }



        fun startSectionEdit(section: String) {
            isEditingExercise = section == "exercise"
            isEditingContent = section == "content"
            isEditingWords = section == "words"
            isEditingQuestions = section == "questions"
        }

        LaunchedEffect(exerciseId) {
            exerciseVm.getExerciseById(exerciseId)
            questionVm.getQuestionsByExerciseId(exerciseId)
            wordsVm.getWordsByExerciseId(exerciseId)
        }

        // --- ERROR / SNACKBAR SYNC ---
        LaunchedEffect(exerciseUi.error) {
            println("Exercise UI Error: ${exerciseUi.error}") // Debug log
            exerciseUi.error?.let {
                snackbarHostState.showSnackbar(it)
                println("Exercise UI Error: ${exerciseUi.error}") // Debug log
            }
        }
        LaunchedEffect(questionUi.error) {
            questionUi.error?.let { snackbarHostState.showSnackbar(it) }
            println("Question UI Error: ${questionUi.error}") // Debug log
        }
        LaunchedEffect(aiUi.error) {
            aiUi.error?.let { snackbarHostState.showSnackbar(it)
            println("AI Question UI Error: ${aiUi.error}") }
        }
        LaunchedEffect(wordsUi.error){
            wordsUi.error?.let { snackbarHostState.showSnackbar(it) }
            println("Exercise UI Error: ${wordsUi.error}")
        }

        // --- SYNC DATA TO DRAFTS ---
        LaunchedEffect(exerciseUi.selectedExercise) {
            exerciseUi.selectedExercise?.let {
                draftName = it.name
                draftDescription = it.description ?: ""
                draftActive = it.isActive
            }
        }

        // Keep drafts in sync when not editing
        LaunchedEffect(
            questionUi.selectedQuestions,
            questionUi.alternatives,
            exerciseUi.selectedContent,
            wordsUi.words
        ) {
            if (!isEditingContent) {
                exerciseUi.selectedContent?.let { contentEx ->
                    contentDraft = ContentDraft(
                        id = contentEx.id,
                        exerciseId = contentEx.exerciseId,
                        contentType = contentEx.contentType,
                        textContent = contentEx.textContent,
                        grammarExplanation = contentEx.grammarExplanation,
                        audioUrl = contentEx.audioUrl
                    )
                }
            }

            if (!isEditingWords) {
                wordDrafts.clear()
                wordsUi.words.forEach { word ->
                    wordDrafts[word.id] = WordDraftState(
                        id = word.id,
                        english = word.english,
                        spanish = word.spanish,
                        phonetic = word.phonetic,
                        description = word.description,
                        isActive = word.isActive ?: false
                    )
                }

                // Limpia estados de expansión para palabras eliminadas
                val currentIds = wordsUi.words.map { it.id }.toSet()
                expandedWordCards.keys.toList().forEach { id ->
                    if (id !in currentIds) expandedWordCards.remove(id)
                }
            }

            if (!isEditingQuestions) {
                questionDrafts.clear()
                questionUi.selectedQuestions.forEach { q ->
                    val domainAlts = questionUi.alternatives[q.id] ?: emptyList()
                    val draftAlts = domainAlts.map {
                        DraftAlternative(
                            id = it.id,
                            text = it.text,
                            isCorrect = it.isCorrect ?: false
                        )
                    }

                    questionDrafts[q.id] = QuestionDraftState(
                        id = q.id,
                        questionText = q.questionText,
                        alternatives = draftAlts,
                        orderQuestion = q.orderQuestion,
                        isActive = q.isActive ?: false
                    )
                }
            }
        }

        fun onSaveExercise() {
            if (draftName.isBlank()) {
                exerciseVm.updateMessage("El nombre del ejercicio es obligatorio")
                return
            }
            exerciseVm.updateExercise(
                exerciseId,
                UpdateExerciseDto(
                    name = draftName,
                    description = draftDescription,
                    isActive = draftActive
                )
            )
            isEditingExercise = false
            scope.launch { snackbarHostState.showSnackbar("Ejercicio actualizado.") }
        }

        fun onSaveContent() {
            if (contentDraft.textContent.isBlank() || contentDraft.grammarExplanation.isBlank()) {
                exerciseVm.updateMessage("Contenido y gramática son obligatorios")
                return
            }
            exerciseVm.updateContent(
                exerciseId,
                UpdateExerciseContentDto(
                    textContent = contentDraft.textContent,
                    grammarExplanation = contentDraft.grammarExplanation,
                    audioUrl = contentDraft.audioUrl
                )
            )
            isEditingContent = false
            scope.launch { snackbarHostState.showSnackbar("Contenido actualizado.") }
        }

        fun onSaveWords() {
            if (wordDrafts.isEmpty()) {
                wordsVm.updateMessage("No hay vocabulario para actualizar")
                return
            }
            wordDrafts.values.forEach { wordDraft ->
                wordsVm.updateWord(
                    wordDraft.id,
                    UpdateWordDto(
                        english = wordDraft.english,
                        spanish = wordDraft.spanish,
                        phonetic = wordDraft.phonetic,
                        description = wordDraft.description,
                        isActive = wordDraft.isActive
                    )
                )
            }
            isEditingWords = false
            scope.launch { snackbarHostState.showSnackbar("Vocabulario actualizado.") }
        }

        fun onSaveQuestions() {
            questionDrafts.values.forEach { draft ->
                if (draft.alternatives.size < 2) {
                    questionVm.updateMessage("Cada pregunta debe tener al menos 2 alternativas.")
                    return
                }
            }

            questionDrafts.values.forEach { draft ->
                questionVm.updateQuestion(
                    draft.id,
                    UpdateQuestionDto(
                        questionText = draft.questionText,
                        isActive = draft.isActive
                    )
                )

                draft.alternatives.values.forEach { alt ->
                    if (alt.id != null) {
                        questionVm.updateAlternativesForQuestion(
                            alt.id,
                            UpdateAlternativeDto(text = alt.text, isCorrect = alt.isCorrect)
                        )
                    } else {
                        questionVm.createAlternativeForQuestion(
                            questionId = draft.id,
                            newAlternative = CreateAlternativeDto(
                                text = alt.text,
                                isCorrect = alt.isCorrect
                            )
                        )
                    }
                }

                val originalAlternatives = questionUi.alternatives[draft.id] ?: emptyList()
                val currentAlternativeIds = draft.alternatives.values.mapNotNull { it.id }.toSet()
                originalAlternatives.forEach { originalAlt ->
                    if (originalAlt.id !in currentAlternativeIds) {
                        questionVm.deleteAlternative(originalAlt.id)
                    }
                }
            }

            isEditingQuestions = false
            scope.launch { snackbarHostState.showSnackbar("Preguntas actualizadas.") }
        }


        EditorLayout(
            actualScreen = "Detalles del Ejercicio",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Card(
                modifier = Modifier

                    .fillMaxSize()
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                    .verticalScroll(rememberScrollState()),

                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
            ) {
                if (exerciseUi.isLoading || questionUi.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF003AB6))
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "📝",
                                fontSize = 28.sp,
                                modifier = Modifier.padding(end = 12.dp, top = 4.dp)
                            )

                            // Title & Description Column
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Name Field
                                    Box(
                                        modifier = Modifier.weight(1f, fill = false)
                                            .padding(end = 8.dp)
                                    ) {
                                        BasicTextField(
                                            value = draftName,
                                            onValueChange = { draftName = it },
                                            readOnly = !isEditingExercise,
                                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = encodeSansFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color(0xFF131313)
                                            ),
                                            decorationBox = { innerTextField ->
                                                if (isEditingExercise && draftName.isEmpty()) {
                                                    Text("Nombre del ejercicio", color = Color.Gray)
                                                }
                                                innerTextField()
                                            },
                                            modifier = if (isEditingExercise) Modifier.background(
                                                Color(0xFFF5F5F5),
                                                RoundedCornerShape(4.dp)
                                            ).padding(4.dp) else Modifier
                                        )
                                    }

                                    ActiveDraftBadge(
                                        isActive = draftActive,
                                        activeLabel = "Publicado",
                                        draftLabel = "Borrador",
                                        clickable = isEditingExercise,
                                        onToggle = { draftActive = !draftActive }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Description Field
                                BasicTextField(
                                    value = draftDescription,
                                    onValueChange = { draftDescription = it },
                                    readOnly = !isEditingExercise,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = jetbrainsMonoFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4A4A4A),
                                        lineHeight = 20.sp
                                    ),
                                    decorationBox = { inner ->
                                        if (isEditingExercise && draftDescription.isEmpty()) {
                                            Text(
                                                "Añadir descripción...",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = if (isEditingExercise) Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                    else Modifier.fillMaxWidth()
                                )
                            }

                            // Edit / Save Actions
                            Column(
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isEditingExercise) {
                                            onSaveExercise()
                                        } else {
                                            startSectionEdit("exercise")
                                        }
                                    },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isEditingExercise) Color(0xFFB8F4C4) else Color(
                                                0xFFF5F5F5
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isEditingExercise) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = if (isEditingExercise) Color(0xFF2D5E3D) else Color(
                                            0xFF003AB6
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isEditingExercise) {
                                    IconButton(
                                        onClick = { isEditingExercise = false },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                Color(0xFFFFD4D4),
                                                RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = "Cancelar",
                                            tint = Color(0xFF8B0000),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isEditingExercise) {
                                    IconButton(
                                        onClick = { onDeleteExercise = true },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                Color(0xFFFFD4D4),
                                                RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFF8B0000),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (onDeleteExercise) {
                                        AlertDialog(
                                            onDismissRequest = {
                                                onDeleteExercise = false
                                                confirmChecked.value = false
                                            },
                                            title = { Text("Confirmar eliminación") },
                                            text = {
                                                Column {
                                                    Text("Marca la casilla y acepta para eliminar este ejercicio.")
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = confirmChecked.value,
                                                            onCheckedChange = {
                                                                confirmChecked.value = it
                                                            },
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = Color(0xFF2D5E3D)
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Estoy seguro/a", fontSize = 14.sp)
                                                    }
                                                }
                                            },
                                            confirmButton = {
                                                Button(
                                                    onClick = {
                                                        if (confirmChecked.value) {
                                                            exerciseVm.deleteExercise(exerciseId)
                                                            onDeleteExercise = false
                                                            confirmChecked.value = false
                                                            navigator?.pop()
                                                        }
                                                    },
                                                    enabled = confirmChecked.value,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFFD4D4)
                                                    )
                                                ) {
                                                    Text("Eliminar", color = Color(0xFF8B0000))
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(
                                                    onClick = {
                                                        onDeleteExercise = false
                                                        confirmChecked.value = false
                                                    }
                                                ) {
                                                    Text("Cancelar")
                                                }
                                            }
                                        )
                                    }


                                }
                            }
                        }

                        // --- DIVIDER ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Contenido",
                                fontFamily = encodeSansFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 8.dp).weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            SectionActionButtons(
                                isEditing = isEditingContent,
                                onEditOrSave = {
                                    if (isEditingContent) onSaveContent() else startSectionEdit("content")
                                },
                                onCancel = { isEditingContent = false },
                                onDelete = if (exerciseUi.selectedContent != null) ({
                                    onDeleteContent = true
                                }) else null
                            )
                        }

                        //Content
                        if (exerciseUi.selectedContent == null) {
                            Text("No hay contenido", color = Color.Gray)
                            TextButton(
                                onClick = { isAddingContent = !isAddingContent },
                                colors = ButtonColors(
                                    contentColor = Color(0xFF2E7D32),
                                    containerColor = Color(0xFFB8F4C4),
                                    disabledContainerColor = Color(0xFFE0E0E0),
                                    disabledContentColor = Color(0xFF9E9E9E)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Agregar contenido")
                            }
                        } else {
                            exerciseUi.selectedContent?.let {
                                ContentEditableRegion(
                                    isEditing = isEditingContent,
                                    draft = contentDraft,
                                    encodeSansFamily = encodeSansFamily,
                                    jetbrainsMonoFamily = jetbrainsMonoFamily
                                )
                            }
                            if (onDeleteContent) {
                                AlertDialog(
                                    onDismissRequest = { onDeleteContent = false },
                                    title = { Text("Eliminar contenido") },
                                    text = { Text("Esta acción eliminará el contenido del ejercicio.") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                exerciseVm.deleteContent(exerciseId)
                                                onDeleteContent = false
                                                isEditingContent = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFFFFD4D4
                                                )
                                            )
                                        ) {
                                            Text("Eliminar", color = Color(0xFF8B0000))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            onDeleteContent = false
                                        }) { Text("Cancelar") }
                                    }
                                )
                            }
                            // --- Vocabulario ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Vocabulario",
                                    fontFamily = encodeSansFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SectionActionButtons(
                                    isEditing = isEditingWords,
                                    onEditOrSave = {
                                        if (isEditingWords) onSaveWords() else startSectionEdit("words")
                                    },
                                    onCancel = { isEditingWords = false }
                                )
                            }
                            TextButton(
                                onClick = { isAddingWord = !isAddingWord },
                                enabled = !isAddingWord,
                                colors = ButtonColors(
                                    contentColor = Color(0xFF2E7D32),
                                    containerColor = Color(0xFFB8F4C4),
                                    disabledContainerColor = Color(0xFFE0E0E0),
                                    disabledContentColor = Color(0xFF9E9E9E)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Agregar vocabulario")
                            }
                            AnimatedVisibility(
                                visible = isAddingWord,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "Nueva palabra",
                                            fontFamily = encodeSansFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )


                                        CustomTextField(
                                            value = newWordEnglish,
                                            onValueChange = { newWordEnglish = it },
                                            label = "Inglés",
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CustomTextField(
                                            value = newWordSpanish,
                                            onValueChange = { newWordSpanish = it },
                                            label = "Español",
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,

                                            )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CustomTextField(
                                            value = newPhonetic,
                                            onValueChange = { newPhonetic = it },
                                            label = "Fonética (opcional)",
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,

                                            )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CustomTextField(
                                            value = newWordDescription,
                                            onValueChange = { newWordDescription = it },
                                            label = "Descripción (opcional)",
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = false,
                                            maxLines = 4,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Button(
                                                onClick = { isAddingWord = false },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFFFD4D4)
                                                )
                                            ) {
                                                Text("Cancelar", color = Color(0xFF8B0000))
                                            }
                                            Button(
                                                onClick = {
                                                    if (newWordEnglish.isBlank() || newWordSpanish.isBlank()) {
                                                        wordsVm.updateMessage("Ingles y español son obligatorios")
                                                        return@Button
                                                    }

                                                    wordsVm.createWord(
                                                        exerciseId,
                                                        CreateWordDto(
                                                            english = newWordEnglish,
                                                            spanish = newWordSpanish,
                                                            phonetic = newPhonetic,
                                                            description = newWordDescription,
                                                            isActive = isActiveWord
                                                        )
                                                    )

                                                    // Mantiene el panel abierto para cargar varias palabras seguidas
                                                    newWordEnglish = ""
                                                    newWordSpanish = ""
                                                    newPhonetic = ""
                                                    newWordDescription = ""
                                                    isActiveWord = false

                                                    //cerrar
                                                    isAddingWord = false
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFB8F4C4)
                                                )
                                            ) {
                                                Text(
                                                    "Guardar Palabra",
                                                    color = Color(0xFF2D5E3D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (wordsUi.words.isEmpty()) {
                                Text("No hay vocabulario asociado", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))


                            } else {

                                wordsUi.words.forEach { word ->
                                    val wordDraft = wordDrafts.getOrPut(word.id) {
                                        WordDraftState(
                                            id = word.id,
                                            english = word.english,
                                            spanish = word.spanish,
                                            phonetic = word.phonetic,
                                            description = word.description,
                                            isActive = word.isActive ?: false
                                        )
                                    }
                                    WordEditableRegion(
                                        isEditing = isEditingWords,
                                        isExpanded = expandedWordCards[word.id] ?: false,
                                        onToggleExpanded = {
                                            expandedWordCards[word.id] =
                                                !(expandedWordCards[word.id] ?: false)
                                        },
                                        draft = wordDraft,
                                        jetbrainsMonoFamily = jetbrainsMonoFamily,
                                        onDelete = {
                                            wordsVm.deleteWord(word.id)
                                            wordDrafts.remove(word.id)
                                            expandedWordCards.remove(word.id)
                                        }
                                    )
                                }
                            }


                            if (exerciseUi.selectedContent != null) {
                                AiQuestionGenerationSection(
                                    contentId = exerciseUi.selectedContent?.id,
                                    aiVm = aiQuestionVm,
                                    onQuestionConfirmed = { confirmResponse ->
                                        questionVm.addConfirmedAiQuestion(
                                            question = confirmResponse.question,
                                            alternatives = confirmResponse.alternatives
                                        )
                                    }
                                )
                            }


                            // --- DIVIDER ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Preguntas",
                                    fontFamily = encodeSansFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SectionActionButtons(
                                    isEditing = isEditingQuestions,
                                    onEditOrSave = {
                                        if (isEditingQuestions) onSaveQuestions() else startSectionEdit(
                                            "questions"
                                        )
                                    },
                                    onCancel = { isEditingQuestions = false }
                                )
                            }
                            if (questionUi.selectedQuestions.isEmpty()) {
                                Text("No hay preguntas", color = Color.Gray)
                            } else {
                                questionUi.selectedQuestions.forEach { question ->
                                    val alt = questionUi.alternatives[question.id] ?: emptyList()
                                    QuestionEditableRegion(
                                        isEditing = isEditingQuestions,
                                        draft = questionDrafts[question.id] ?: QuestionDraftState(
                                            id = question.id,
                                            questionText = question.questionText,
                                            alternatives = alt.map {
                                                DraftAlternative(
                                                    id = it.id,
                                                    text = it.text,
                                                    isCorrect = it.isCorrect ?: false
                                                )
                                            },
                                            orderQuestion = question.orderQuestion,
                                            isActive = question.isActive ?: false
                                        ),
                                        jetbrainsMonoFamily = jetbrainsMonoFamily,
                                        onDeleteQuestion = {
                                            questionVm.deleteQuestionAndAlternatives(question.id)
                                            questionDrafts.remove(question.id)
                                        }
                                    )
                                }
                            }
                            TextButton(
                                onClick = { isAddingQuestion = !isAddingQuestion },
                                enabled = !isAddingQuestion,
                                colors = ButtonColors(
                                    contentColor = Color(0xFF2E7D32),
                                    containerColor = Color(0xFFB8F4C4),
                                    disabledContainerColor = Color(0xFFE0E0E0),
                                    disabledContentColor = Color(0xFF9E9E9E)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Agregar pregunta")
                            }

                        }

                        AnimatedVisibility(
                            visible = isAddingContent,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Nueva pregunta",
                                        fontFamily = encodeSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    OutlinedTextField(
                                        value = contentText,
                                        onValueChange = { contentText = it },
                                        label = { Text("Explicación / Contexto") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = contentText.isBlank()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contentGrammar,
                                        onValueChange = { contentGrammar = it },
                                        label = { Text("Gramática") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = contentGrammar.isBlank()
                                    )

                                    OutlinedTextField(
                                        value = contentUrlAudio,
                                        onValueChange = { contentUrlAudio = it },
                                        label = { Text("URL de audio (opcional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontFamily = jetbrainsMonoFamily,
                                            fontSize = 14.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { isAddingContent = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD4D4)
                                            )
                                        ) {
                                            Text("Cancelar", color = Color(0xFF8B0000))
                                        }
                                        Button(
                                            onClick = {


                                                exerciseVm.createContent(
                                                    exerciseId,
                                                    CreateExerciseContentDto(
                                                        contentType = ContentType.READING,
                                                        textContent = contentText,
                                                        grammarExplanation = contentGrammar,
                                                        audioUrl = contentUrlAudio
                                                    )
                                                )


                                                // Reset fields
                                                contentText = ""
                                                contentGrammar = ""
                                                contentUrlAudio = ""

                                                isAddingContent = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFFB8F4C4
                                                )
                                            )
                                        ) {
                                            Text("Guardar Contenido", color = Color(0xFF2D5E3D))
                                        }
                                    }

                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isAddingQuestion,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = questionText,
                                        onValueChange = { questionText = it },
                                        label = { Text("Texto de la pregunta") },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontFamily = jetbrainsMonoFamily,
                                            fontSize = 14.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        isError = questionText.isBlank()
                                    )

                                    AnimatedVisibility(visible = true) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xFFF0F4F8),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                "Configuración de Alternativas",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF003AB6)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Lista de alternativas agregadas
                                            if (alternatives.isEmpty()) {
                                                Text(
                                                    "Agrega al menos 2 alternativas.",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }

                                            alternatives.forEachIndexed { index, alt ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    RadioButton(
                                                        selected = alt.isCorrect == true,
                                                        onClick = {
                                                            // Marcar esta como correcta y las demas false
                                                            alternatives =
                                                                alternatives.mapIndexed { i, a ->
                                                                    a.copy(isCorrect = i == index)
                                                                }
                                                        },
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = Color(
                                                                0xFF2E7D32
                                                            )
                                                        )
                                                    )
                                                    Text(
                                                        alt.text,
                                                        modifier = Modifier.weight(1f),
                                                        fontFamily = jetbrainsMonoFamily,
                                                        fontSize = 13.sp
                                                    )
                                                    IconButton(onClick = {
                                                        val list = alternatives.toMutableList()
                                                        list.removeAt(index)
                                                        alternatives = list
                                                    }) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Red,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(color = Color.White)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            // Input para nueva alternativa
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedTextField(
                                                    value = newAltText,
                                                    onValueChange = { newAltText = it },
                                                    placeholder = { Text("Texto de alternativa") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    textStyle = TextStyle(
                                                        fontSize = 13.sp,
                                                        fontFamily = jetbrainsMonoFamily
                                                    ),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    )
                                                )
                                                IconButton(
                                                    onClick = {
                                                        if (newAltText.isNotBlank()) {
                                                            // Si es la primera, marcarla como correcta por defecto (para asegurar que haya una)
                                                            val isCorrect = alternatives.isEmpty()
                                                            alternatives =
                                                                alternatives + CreateAlternativeDto(
                                                                    newAltText,
                                                                    isCorrect
                                                                )
                                                            newAltText = ""
                                                        }
                                                    },
                                                    enabled = newAltText.isNotBlank()
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Agregar",
                                                        tint = Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { isAddingQuestion = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD4D4)
                                            )
                                        ) {
                                            Text("Cancelar", color = Color(0xFF8B0000))
                                        }
                                        Button(
                                            onClick = {

                                                // Validación rápida de alternativas
                                                if (alternatives.size < 2) {
                                                    questionVm.updateMessage("Agrega al menos 2 alternativas.")
                                                    return@Button
                                                }

                                                // CAPTURA IMPORTANTE: Copiamos la lista antes de que se limpie más abajo
                                                val alternativesToCreate = alternatives.toList()

                                                exerciseUi.selectedContent?.let {
                                                    questionVm.createQuestion(
                                                        exerciseContent = it.id,
                                                        CreateQuestionDto(
                                                            questionText = questionText,
                                                            orderQuestion = null,
                                                            isActive = true
                                                        )
                                                    ) { questionId ->
                                                        // Crear alternativas asociadas a esta pregunta usando la lista capturada
                                                        alternativesToCreate.forEach { alt ->
                                                            questionVm.createAlternativeForQuestion(
                                                                questionId = questionId,
                                                                CreateAlternativeDto(
                                                                    text = alt.text,
                                                                    isCorrect = alt.isCorrect
                                                                        ?: false
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                questionText = ""
                                                alternatives = emptyList() // Reset alternatives

                                                isAddingQuestion = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFFB8F4C4
                                                )
                                            )
                                        ) {
                                            Text("Guardar Contenido", color = Color(0xFF2D5E3D))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ContentEditableRegion(
    isEditing: Boolean,
    draft: ContentDraft,
    encodeSansFamily: FontFamily,
    jetbrainsMonoFamily: FontFamily,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // --- 1. Text Content ---
        Column {
            Text(
                "Explicación / Contexto",
                fontFamily = encodeSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF003AB6)
            )
            if (isEditing) {
                CustomTextField(
                    value = draft.textContent,
                    onValueChange = { draft.textContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                )
            } else {
                Text(draft.textContent, fontFamily = jetbrainsMonoFamily, fontSize = 14.sp)
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // --- 2. Grammar Explanation ---
        Column {
            Text(
                "Gramática",
                fontFamily = encodeSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Gray
            )
            if (isEditing) {
                CustomTextField(
                    value = draft.grammarExplanation,
                    onValueChange = { draft.grammarExplanation = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                )
            } else {
                Text(
                    draft.grammarExplanation.ifBlank { "Sin explicación" },
                    fontFamily = jetbrainsMonoFamily,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        // --- 3. Audio URL ---
        Column {
            Text(
                "Audio URL (Opcional)",
                fontFamily = encodeSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Gray
            )
            if (isEditing) {
                CustomTextField(
                    value = draft.audioUrl ?: "",
                    onValueChange = { draft.audioUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    placeholderText = "https://..."
                )
            } else {
                Text(
                    draft.audioUrl?.ifBlank { "Sin audio" } ?: "Sin audio",
                    fontFamily = jetbrainsMonoFamily,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))
    }
}

@Composable
fun WordEditableRegion(
    draft: WordDraftState,
    isEditing: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    jetbrainsMonoFamily: FontFamily,
    onDelete: (() -> Unit)? = null,
) {
    val showDetails = isExpanded

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        CustomTextField(
                            value = draft.english,
                            label = "Ingles",
                            onValueChange = { draft.english = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontFamily = jetbrainsMonoFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            placeholderText = "Palabra en ingles"
                        )
                    }
                } else {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = draft.english.ifBlank { "(Sin palabra)" },
                        fontFamily = jetbrainsMonoFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (draft.isActive) Color(0xFF131313) else Color(0xFF4B5563)
                    )
                }

                ActiveDraftBadge(
                    isActive = draft.isActive,
                    activeLabel = "Activa",
                    draftLabel = "Borrador",
                    clickable = isEditing,
                    onToggle = { draft.isActive = !draft.isActive }
                )

                IconButton(onClick = onToggleExpanded, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showDetails) "Contraer" else "Expandir",
                        tint = Color(0xFF003AB6),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isEditing && onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar palabra",
                            tint = Color(0xFF8B0000),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (showDetails && isEditing) {
                CustomTextField(
                    value = draft.spanish,
                    label = "Espanol",
                    onValueChange = { draft.spanish = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholderText = "Significado en espanol"
                )
                CustomTextField(
                    value = draft.phonetic ?: "",
                    label = "Fonetica (opcional)",
                    onValueChange = { draft.phonetic = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholderText = "Ej: /nɜːs/"
                )
                CustomTextField(
                    value = draft.description ?: "",
                    label = "Descripcion (opcional)",
                    onValueChange = { draft.description = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    placeholderText = "Uso o contexto"
                )
            } else if (showDetails) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                WordDetailRow("Espanol", draft.spanish.ifBlank { "-" }, jetbrainsMonoFamily)
                WordDetailRow(
                    "Fonetica",
                    draft.phonetic?.ifBlank { "Sin fonetica" } ?: "Sin fonetica",
                    jetbrainsMonoFamily
                )
                WordDetailRow(
                    "Descripcion",
                    draft.description?.ifBlank { "Sin descripcion" } ?: "Sin descripcion",
                    jetbrainsMonoFamily
                )
            }
        }

    }
}


@Composable
fun QuestionEditableRegion(
    draft: QuestionDraftState,
    isEditing: Boolean,
    jetbrainsMonoFamily: FontFamily,
    onDeleteQuestion: (() -> Unit)? = null,
) {
    var newAltText by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp)
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = draft.questionText,
                        onValueChange = { draft.questionText = it },
                        singleLine = false,
                        textStyle = TextStyle(
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (draft.isActive) Color(0xFF131313) else Color.Gray
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F6F8), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (draft.questionText.isBlank()) {
                                    Text(
                                        text = "Texto de la pregunta",
                                        fontFamily = jetbrainsMonoFamily,
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        draft.questionText,
                        fontFamily = jetbrainsMonoFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (draft.isActive) Color(0xFF131313) else Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                ActiveDraftBadge(
                    isActive = draft.isActive,
                    activeLabel = "Activa",
                    draftLabel = "Borrador",
                    clickable = isEditing,
                    onToggle = { draft.isActive = !draft.isActive }
                )

                if (isEditing && onDeleteQuestion != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteQuestion) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar pregunta",
                            tint = Color(0xFF8B0000),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

        }

        // --- 4. Alternatives Area ---
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isEditing) {
                // Renderizar alternativas del Draft
                draft.alternatives.values.forEach { alt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = alt.isCorrect,
                            onClick = {
                                // Update visual state in map
                                draft.alternatives.values.forEach { it.isCorrect = false }
                                alt.isCorrect = true
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
                        )

                        Box(
                            modifier = Modifier.weight(1f, fill = false)
                                .padding(end = 8.dp)
                        ) {
                            CustomTextField(
                                value = alt.text,
                                onValueChange = { newText ->
                                    alt.text = newText
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Boton eliminar (opcional, solo visual por ahora)
                        IconButton(onClick = {
                            draft.alternatives.remove(alt.tempId)
                        }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Quitar",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                // Input para nueva alternativa
                if(draft.alternatives.size <= 5){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.padding(12.dp)){}
                        CustomTextField(
                            value = newAltText,
                            onValueChange = { newAltText = it },
                            placeholderText = "Texto de nueva alternativa",
                            singleLine = true,
                        )
                        IconButton(
                            onClick = {
                                if (newAltText.isNotBlank()) {
                                    // Agregar al mapa del draft directamente
                                    val isCorrect = draft.alternatives.isEmpty()
                                    val newDraftAlt = DraftAlternative(
                                        id = null, // Marca como nuevo
                                        text = newAltText,
                                        isCorrect = isCorrect
                                    )
                                    draft.alternatives[newDraftAlt.tempId] = newDraftAlt
                                    newAltText = ""
                                }
                            },
                            enabled = newAltText.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(disabledContentColor = Color.Gray)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = Color(0xFF003AB6)
                            )
                        }
                    }
                }


            } else {
                // Read Mode
                draft.alternatives.values.forEach { alt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (alt.isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (alt.isCorrect && draft.isActive) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alt.text,
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 14.sp,
                            color = if (alt.isCorrect && draft.isActive) Color(0xFF2E7D32) else Color(
                                0xFF131313
                            )
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun ActiveDraftBadge(
    isActive: Boolean,
    activeLabel: String,
    draftLabel: String,
    clickable: Boolean = false,
    onToggle: () -> Unit = {},
) {
    val badgeColor = if (isActive) Color(0xFFB8F4C4) else Color(0xFFFFD4D4)
    val textColor = if (isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000)

    Badge(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(enabled = clickable, onClick = onToggle),
        containerColor = badgeColor,
        contentColor = textColor
    ) {
        Text(if (isActive) activeLabel else draftLabel, fontSize = 12.sp)
    }
}

@Composable
fun WordDetailRow(
    label: String,
    value: String,
    jetbrainsMonoFamily: FontFamily,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(92.dp)
        )
        Text(
            text = value,
            fontFamily = jetbrainsMonoFamily,
            fontSize = 13.sp,
            color = Color(0xFF131313),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SectionActionButtons(
    isEditing: Boolean,
    onEditOrSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onEditOrSave, modifier = Modifier.size(22.dp)) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = if (isEditing) "Guardar" else "Editar",
                tint = if (isEditing) Color(0xFF2D5E3D) else Color(0xFF003AB6),
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = isEditing) {
            IconButton(onClick = onCancel, modifier = Modifier.size(22.dp)) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Cancelar",
                    tint = Color(0xFF8B0000),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFF8B0000),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

