package org.example.project.screens.admindScreens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import kotlinx.coroutines.launch
import org.example.project.components.AppLayout
import org.example.project.dtos.ContentType
import org.example.project.dtos.CreateAlternativeDto
import org.example.project.dtos.CreateExerciseContentDto
import org.example.project.dtos.CreateQuestionDto
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateExerciseContentDto
import org.example.project.dtos.UpdateExerciseDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.QuestionViewModel
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
        val exerciseVm = rememberScreenModel {
            ExercisesViewModel(
                RepositoryProvider.exerciseRepo,
                unitId,
                exerciseId
            )
        }
        val questionVm =
            rememberScreenModel { QuestionViewModel(RepositoryProvider.questionRepo, exerciseId) }
        val exerciseUi by exerciseVm.state.collectAsState()
        val questionUi by questionVm.state.collectAsState()

        var selectedIndex by remember { mutableStateOf(3) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
        val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

        //Content
        var isAddingContent by remember { mutableStateOf(false) }
        var isEditingContent by remember { mutableStateOf(false) }
        var contentText by remember { mutableStateOf("") }
        var contentGrammar by remember { mutableStateOf("") }
        var contentUrlAudio by remember { mutableStateOf("") }
        var contentDraft by remember {
            mutableStateOf<ContentDraft>(
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


        //Question
        var isAddingQuestion by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }
        var questionText by remember { mutableStateOf("") }

        // Estado para nuevas alternativas
        var alternatives by remember { mutableStateOf(listOf<CreateAlternativeDto>()) }
        var newAltText by remember { mutableStateOf("") }

        // --- EXERCISE DRAFTS ---
        var draftName by remember { mutableStateOf("") }
        var draftDescription by remember { mutableStateOf("") }
        var draftActive by remember { mutableStateOf(false) }

        // ---Exercise Content Draft---


        // --- QUESTION DRAFTS ---
        val questionDrafts = remember { mutableStateMapOf<Int, QuestionDraftState>() }


        // Estados para dropdowns de exercise
        var statusMenuExpanded by remember { mutableStateOf(false) }

        LaunchedEffect(exerciseId) {
            exerciseVm.getExerciseById(exerciseId)
            questionVm.getQuestionsByExerciseId(exerciseId)

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
            exerciseUi.selectedContent
        ) {
            if (!isEditing) {

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

        // --- SAVE FUNCTION ---
        fun onSaveAll() {
            // Validations
            if (draftName.isBlank() && draftDescription.isBlank() && contentDraft.textContent.isBlank() && contentDraft.grammarExplanation.isBlank()
            ) {
                exerciseVm.updateMessage("Algunos campos son obligatorios")
                return
            }

            // Check questions validity
            questionDrafts.values.forEach { draft ->
                if (draft.alternatives.size < 2) {
                    questionVm.updateMessage("Cada pregunta debe tener al menos 2 alternativas.")
                    return
                }
            }

            // Execute Updates
            exerciseVm.updateExercise(
                exerciseId,
                UpdateExerciseDto(
                    name = draftName,
                    description = draftDescription,
                    isActive = draftActive
                )
            )
            exerciseVm.updateContent(
                exerciseId,
                UpdateExerciseContentDto(
                    textContent = contentDraft.textContent,
                    grammarExplanation = contentDraft.grammarExplanation,
                    audioUrl = contentDraft.audioUrl
                )
            )


            questionDrafts.values.forEach { draft ->
                questionVm.updateQuestion(
                    draft.id, UpdateQuestionDto(
                        questionText = draft.questionText,
                        isActive = draft.isActive
                        )
                )

                // Alternatives handling
                draft.alternatives.values.forEach { alt ->
                    if (alt.id != null) {
                        // Es una alternativa existente -> Actualizar
                        questionVm.updateAlternativesForQuestion(
                            alt.id,
                            UpdateAlternativeDto(text = alt.text, isCorrect = alt.isCorrect)
                        )
                    } else {
                        // Es una alternativa nueva -> Crear
                        questionVm.createAlternativeForQuestion(
                            questionId = draft.id,
                            newAlternative = CreateAlternativeDto(
                                text = alt.text,
                                isCorrect = alt.isCorrect
                            )
                        )
                    }
                }

                // Lógica para borrar alternativas removidas del draft
                val originalAlternatives = questionUi.alternatives[draft.id] ?: emptyList()
                val currentAlternativeIds = draft.alternatives.values.mapNotNull { it.id }.toSet()

                originalAlternatives.forEach { originalAlt ->
                    if (originalAlt.id !in currentAlternativeIds) {
                        questionVm.deleteAlternative(originalAlt.id)
                    }
                }
            }
            isEditing = false
            scope.launch { snackbarHostState.showSnackbar("Cambios guardados exitosamente.") }
            // IMPORTANTE: No recargar aquí para mantener los datos editados visibles
            // exerciseVm.getExerciseById(exerciseId) 
        }


        AppLayout(
            actualScreen = "Detalles del Ejercicio",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            readOnly = !isEditing,
                                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = encodeSansFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color(0xFF131313)
                                            ),
                                            decorationBox = { innerTextField ->
                                                if (isEditing && draftName.isEmpty()) {
                                                    Text("Nombre del ejercicio", color = Color.Gray)
                                                }
                                                innerTextField()
                                            },
                                            modifier = if (isEditing) Modifier.background(
                                                Color(0xFFF5F5F5),
                                                RoundedCornerShape(4.dp)
                                            ).padding(4.dp) else Modifier
                                        )
                                    }

                                    // Badge & Status Dropdown
                                    Box {
                                        val isActive = draftActive
                                        val badgeColor =
                                            if (isActive) Color(0xFFB8F4C4) else Color(0xFFFFD4D4)
                                        val textColor =
                                            if (isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000)

                                        if (!isEditing) {
                                            Badge(
                                                containerColor = badgeColor,
                                                contentColor = textColor
                                            ) {
                                                Text(
                                                    if (isActive) "Publicado" else "Borrador",
                                                    fontSize = 12.sp
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .background(
                                                        badgeColor,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { statusMenuExpanded = true }
                                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    if (isActive) "Publicado" else "Borrador",
                                                    fontSize = 12.sp,
                                                    color = textColor
                                                )
                                                Icon(
                                                    Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = textColor
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = statusMenuExpanded,
                                                onDismissRequest = { statusMenuExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Publicado") },
                                                    onClick = {
                                                        draftActive = true
                                                        statusMenuExpanded = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Borrador") },
                                                    onClick = {
                                                        draftActive = false
                                                        statusMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Description Field
                                BasicTextField(
                                    value = draftDescription,
                                    onValueChange = { draftDescription = it },
                                    readOnly = !isEditing,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = jetbrainsMonoFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4A4A4A),
                                        lineHeight = 20.sp
                                    ),
                                    decorationBox = { inner ->
                                        if (isEditing && draftDescription.isEmpty()) {
                                            Text(
                                                "Añadir descripción...",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = if (isEditing) Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                    else Modifier.fillMaxWidth()
                                )
                            }

                            // Edit / Save Actions
                            Column(
                                modifier = Modifier.padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isEditing) {
                                            onSaveAll()
                                        } else {
                                            isEditing = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isEditing) Color(0xFFB8F4C4) else Color(0xFFF5F5F5),
                                            RoundedCornerShape(20.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = if (isEditing) Color(0xFF2D5E3D) else Color(
                                            0xFF003AB6
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isEditing) {
                                    IconButton(
                                        onClick = { isEditing = false },
                                        modifier = Modifier
                                            .padding(top = 8.dp)
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

                                AnimatedVisibility(visible = isEditing) {
                                    IconButton(
                                        onClick = { /* Lógica de eliminar ejercicio */ },
                                        modifier = Modifier
                                            .padding(top = 8.dp)
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
                                }
                            }
                        }

                        // --- DIVIDER ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Contenido",
                                fontFamily = encodeSansFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 8.dp).weight(1f))
                        }

                        // --- QUESTION SECTION ---
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
                                    isEditing = isEditing,
                                    draft = contentDraft,
                                    encodeSansFamily = encodeSansFamily,
                                    jetbrainsMonoFamily = jetbrainsMonoFamily
                                )
                            }
                            // --- DIVIDER ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            }
                            if (questionUi.selectedQuestions.isEmpty()) {
                                Text("No hay preguntas", color = Color.Gray)
                            } else {
                                questionUi.selectedQuestions.forEach { question ->
                                    val alt = questionUi.alternatives[question.id] ?: emptyList()
                                    QuestionEditableRegion(
                                        isEditing = isEditing,
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
                                        jetbrainsMonoFamily = jetbrainsMonoFamily
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
                OutlinedTextField(
                    value = draft.textContent,
                    onValueChange = { draft.textContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
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
                OutlinedTextField(
                    value = draft.grammarExplanation,
                    onValueChange = { draft.grammarExplanation = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
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
                OutlinedTextField(
                    value = draft.audioUrl ?: "",
                    onValueChange = { draft.audioUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    placeholder = { Text("https://...") }
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
fun QuestionEditableRegion(
    draft: QuestionDraftState,
    isEditing: Boolean,
    jetbrainsMonoFamily: FontFamily,
) {
    var newAltText by remember { mutableStateOf("") }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = false)
                        .padding(end = 8.dp)
                ){
                    OutlinedTextField(
                        value = draft.questionText,
                        onValueChange = { draft.questionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                //modificar badge
                Box {
                    val isActive = draft.isActive
                    val badgeColor =
                        if (isActive) Color(0xFFB8F4C4) else Color(0xFFFFD4D4)
                    val textColor =
                        if (isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000)

                    if (!isEditing) {
                        Badge(
                            containerColor = badgeColor,
                            contentColor = textColor
                        ) {
                            Text(
                                if (isActive) "Activa" else "Borrador",
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .background(
                                    badgeColor,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { statusMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isActive) "Activa" else "Borrador",
                                fontSize = 12.sp,
                                color = textColor
                            )
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = textColor
                            )
                        }
                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Publicado") },
                                onClick = {
                                    draft.isActive = true
                                    statusMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Borrador") },
                                onClick = {
                                    draft.isActive = false
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = false)
                        .padding(end = 8.dp)
                ){
                    Text(
                        draft.questionText,
                        fontFamily = jetbrainsMonoFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (draft.isActive) Color(0xFF131313) else Color.Gray
                    )
                }

                Badge(
                    modifier = Modifier.padding(4.dp),
                    containerColor = if (draft.isActive) Color(0xFFB8F4C4) else Color(0xFFFFD4D4),
                    contentColor = if (draft.isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000)
                ) {
                    Text(
                        if (draft.isActive) "Activa" else "Borrador",
                        fontSize = 12.sp
                    )
                }

            }
        }

        // --- 4. Alternatives Area ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEditing) {
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
                        ),
                        isError = newAltText.isBlank()
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
                        enabled = newAltText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Color(0xFF003AB6)
                        )
                    }
                }

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

                        OutlinedTextField(
                            value = alt.text,
                            onValueChange = { newText ->
                                alt.text = newText
                            },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.Gray,
                                selectionColors = TextSelectionColors(
                                    handleColor = Color.Gray,
                                    backgroundColor = Color(0xFFB8F4C4).copy(alpha = 0.5f)
                                )
                            )
                        )

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