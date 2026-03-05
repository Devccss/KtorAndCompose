package org.example.project.screens.admindScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import kotlinx.coroutines.launch
import org.example.project.components.AppLayout
import org.example.project.dtos.AlternativesDto
import org.example.project.dtos.CreateAlternativeDto
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.CreateQuestionDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.TypeQuestion
import org.example.project.dtos.TypeTextExercise
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateExerciseDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.QuestionViewModel
import org.jetbrains.compose.resources.Font

// Local state holder for Question Edits
// Se cambia de data class a class con propiedades delegadas (mutableStateOf) para que Compose detecte los cambios.
class QuestionDraftState(
    val id: Int,
    textContent: String,
    grammarExplanation: String,
    questionText: String,
    typeQuestion: TypeQuestion,
    alternatives: List<AlternativesDto>
) {
    var textContent by mutableStateOf(textContent)
    var grammarExplanation by mutableStateOf(grammarExplanation)
    var questionText by mutableStateOf(questionText)
    var typeQuestion by mutableStateOf(typeQuestion)
    var alternatives by mutableStateOf(alternatives)
}

class ExercisesDetailsScreen(private val exerciseId: Int, private val unitId: Int) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
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

        var isAddingQuestion by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        // --- EXERCISE DRAFTS ---
        var draftName by remember { mutableStateOf("") }
        var draftDescription by remember { mutableStateOf("") }
        var draftActive by remember { mutableStateOf(false) }

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
            println( "Exercise UI Error: ${exerciseUi.error}") // Debug log
            exerciseUi.error?.let { snackbarHostState.showSnackbar(it) }
        }
        LaunchedEffect(questionUi.error) {
            questionUi.error?.let { snackbarHostState.showSnackbar(it) }
            println( "Question UI Error: ${questionUi.error}") // Debug log
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
        LaunchedEffect(questionUi.selectedQuestions, questionUi.alternatives, isEditing) {
            if (!isEditing) {
                questionUi.selectedQuestions.forEach { q ->
                    questionDrafts[q.id] = QuestionDraftState(
                        id = q.id,
                        textContent = q.textContent,
                        grammarExplanation = q.grammarExplanation,
                        questionText = q.questionText,
                        typeQuestion = q.typeQuestion,
                        alternatives = questionUi.alternatives[q.id] ?: emptyList()
                    )
                }
            }
        }

        // --- SAVE FUNCTION ---
        fun onSaveAll() {
            // Validations
            if (draftName.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Nombre del ejercicio es obligatorio") }
                return
            }

            // Check questions validity
            var questionsValid = true
            questionDrafts.values.forEach { draft ->
                if (draft.textContent.isBlank() || draft.questionText.isBlank()) {
                    questionsValid = false
                }
                if (draft.typeQuestion == TypeQuestion.ALTERNATIVE && draft.alternatives.isEmpty()) {
                    scope.launch { snackbarHostState.showSnackbar("La pregunta de alternativas debe tener minimo dos opciones") }
                    return
                }
            }
            if (!questionsValid) {
                scope.launch { snackbarHostState.showSnackbar("Campos de texto de pregunta no pueden estar vacíos") }
                return
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

            questionDrafts.values.forEach { draft ->
                questionVm.updateQuestion(
                    draft.id, UpdateQuestionDto(
                        textContent = draft.textContent,
                        grammarExplanation = draft.grammarExplanation,
                        questionText = draft.questionText,
                        typeQuestion = draft.typeQuestion
                    )
                )
                // Alternatives update
                draft.alternatives.forEach { alt ->
                    questionVm.updateAlternativesForQuestion(
                        alt.id,
                        UpdateAlternativeDto(text = alt.text, isCorrect = alt.isCorrect)
                    )
                }
            }
            isEditing = false
            scope.launch { snackbarHostState.showSnackbar("Cambios guardados exitosamente") }
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
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
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
                                            value = if (isEditing) draftName else exerciseUi.selectedExercise?.name
                                                ?: "",
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
                                        val isActive =
                                            if (isEditing) draftActive else exerciseUi.selectedExercise?.isActive == true
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
                                    value = if (isEditing) draftDescription else exerciseUi.selectedExercise?.description
                                        ?: "",
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

                                AnimatedVisibility(visible = isEditing){
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
                                "Contenido y Preguntas",
                                fontFamily = encodeSansFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 8.dp).weight(1f))
                        }

                        // --- QUESTION SECTION ---
                        if (questionUi.selectedQuestions.isEmpty()) {
                            Text("No hay preguntas cargadas.", color = Color.Gray)
                            TextButton(
                                onClick = { isAddingQuestion = !isAddingQuestion },
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
                        } else {
                            questionUi.selectedQuestions.forEach { question ->
                                val draft = questionDrafts[question.id]
                                if (draft != null) {
                                    QuestionEditableRegion(
                                        draft = draft,
                                        isEditing = isEditing,
                                        encodeSansFamily = encodeSansFamily,
                                        jetbrainsMonoFamily = jetbrainsMonoFamily,
                                        onAlert = { msg ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    msg
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        AddQuestionSection(
                            isAdding = isAddingQuestion,
                            onAddingChange = { isAddingQuestion = it },
                            onAdd = { newQuestion, newAlternatives ->
                                questionVm.createQuestion(exerciseId, newQuestion) { createdId ->
                                    // Una vez creada la pregunta, creamos sus alternativas
                                    newAlternatives.forEach { alt ->
                                        questionVm.createAlternativeForQuestion(createdId, alt)
                                    }
                                }
                                isAddingQuestion = false
                            },
                            encodeSansFamily = encodeSansFamily,
                            jetbrainsMonoFamily = jetbrainsMonoFamily,
                            onAddAlternatives = { _, _ -> /* Deprecated/Unused in this flow */ }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionSection(
    isAdding: Boolean,
    onAddingChange: (Boolean) -> Unit = {},
    onAdd: (CreateQuestionDto, List<CreateAlternativeDto>) -> Unit,
    onAddAlternatives: (Int, List<CreateAlternativeDto>) -> Unit,
    encodeSansFamily: FontFamily,
    jetbrainsMonoFamily: FontFamily
) {
    var textContent by remember { mutableStateOf("") }
    var grammarExplanation by remember { mutableStateOf("") }
    var typeQuestion by remember { mutableStateOf<TypeQuestion?>(null) }
    var questionText by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }
    var typeSelect by remember { mutableStateOf(false) }

    // Estado para nuevas alternativas
    var alternatives by remember { mutableStateOf(listOf<CreateAlternativeDto>()) }
    var newAltText by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isAdding,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = Modifier.verticalScroll(rememberScrollState())
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
                Text("Nueva pregunta", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = textContent ?: "",
                    onValueChange = { textContent = it },
                    label = { Text("Explicación / Contexto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = textContent.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = grammarExplanation ?: "",
                    onValueChange = { grammarExplanation = it },
                    label = { Text("Gramática") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = grammarExplanation.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = typeSelect,
                    onExpandedChange = { typeSelect = !typeSelect },
                ) {

                    OutlinedTextField(
                        value = typeQuestion?.name ?: "",
                        onValueChange = { },
                        label = { Text("Tipo de pregunta") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = typeSelect
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(),
                        isError = true,
                    )

                    ExposedDropdownMenu(
                        expanded = typeSelect,
                        onDismissRequest = { typeSelect = false },
                    ) {
                        TypeQuestion.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    typeQuestion = it
                                    typeSelect = false
                                }
                            )
                        }
                    }
                }
                
                // --- SECCION DE ALTERNATIVAS (Solo si es tipo Alternativa) ---
                AnimatedVisibility(visible = typeQuestion == TypeQuestion.ALTERNATIVE) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F4F8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Configuración de Alternativas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF003AB6))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Lista de alternativas agregadas
                        if (alternatives.isEmpty()) {
                            Text("Agrega al menos 2 alternativas.", fontSize = 12.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
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
                                        alternatives = alternatives.mapIndexed { i, a ->
                                            a.copy(isCorrect = i == index)
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
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
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
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
                                textStyle = TextStyle(fontSize = 13.sp, fontFamily = jetbrainsMonoFamily),
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
                                        alternatives = alternatives + CreateAlternativeDto(newAltText, isCorrect)
                                        newAltText = ""
                                    }
                                },
                                enabled = newAltText.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color(0xFF003AB6))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = audioUrl ?: "",
                    onValueChange = { audioUrl = it },
                    label = { Text("URL de audio (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = questionText ?: "",
                    onValueChange = { questionText = it },
                    label = { Text("Texto de la pregunta") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = jetbrainsMonoFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    isError = questionText.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onAddingChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD4D4)
                        )
                    ) {
                        Text("Cancelar", color = Color(0xFF8B0000))
                    }
                    Button(
                        onClick = {

                            // Validación rápida de alternativas
                            if (typeQuestion == TypeQuestion.ALTERNATIVE) {
                                if (alternatives.size < 2) {
                                    // TODO: Mostrar error visual
                                    return@Button
                                }
                                if (alternatives.none { it.isCorrect == true }) {
                                    return@Button
                                }
                            }

                            onAdd(
                                CreateQuestionDto(
                                    textContent = textContent,
                                    grammarExplanation = grammarExplanation,
                                    questionText = questionText,
                                    typeQuestion = typeQuestion ?: TypeQuestion.OPEN,
                                    audioUrl = audioUrl.ifBlank { null },
                                    isActive = false,
                                    typeText = TypeTextExercise.NORMAL,
                                    orderQuestion = null
                                ),
                                alternatives // Pasamos las alternativas
                            )

                            // Reset fields
                            textContent = ""
                            grammarExplanation = ""
                            questionText = ""
                            audioUrl = ""
                            typeQuestion = null
                            alternatives = emptyList() // Reset alternatives

                            onAddingChange(false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFB8F4C4
                            )
                        )
                    ) {
                        Text("Guardar Ejercicio", color = Color(0xFF2D5E3D))
                    }
                }
            }
        }
    }
}


@Composable
fun QuestionEditableRegion(
    draft: QuestionDraftState,
    isEditing: Boolean,
    encodeSansFamily: FontFamily,
    jetbrainsMonoFamily: FontFamily,
    onAlert: (String) -> Unit
) {

    // Alert State for Type Change
    var showTypeChangeAlert by remember { mutableStateOf(false) }
    var pendingTypeChange by remember { mutableStateOf<TypeQuestion?>(null) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    if (showTypeChangeAlert) {
        AlertDialog(
            onDismissRequest = { showTypeChangeAlert = false },
            title = { Text("¿Cambiar tipo de pregunta?") },
            text = { Text("Si cambias a Pregunta Abierta, se eliminarán las alternativas existentes. ¿Continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingTypeChange?.let {
                            draft.typeQuestion = it
                        }
                        showTypeChangeAlert = false
                    }
                ) { Text("Confirmar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showTypeChangeAlert = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
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

        // --- 3. Question & Type ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pregunta",
                fontFamily = encodeSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Black
            )

            // Type Selector
            if (isEditing) {
                Box {
                    Button(
                        onClick = { typeMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1565C0)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            if (draft.typeQuestion == TypeQuestion.ALTERNATIVE) "Alternativas" else "Abierta",
                            fontSize = 12.sp
                        )
                        Icon(Icons.Default.ExpandMore, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Alternativas") },
                            onClick = {
                                typeMenuExpanded = false
                                if (draft.typeQuestion != TypeQuestion.ALTERNATIVE) {
                                    draft.typeQuestion = TypeQuestion.ALTERNATIVE
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Abierta") },
                            onClick = {
                                typeMenuExpanded = false
                                if (draft.typeQuestion == TypeQuestion.ALTERNATIVE && draft.alternatives.isNotEmpty()) {
                                    pendingTypeChange = TypeQuestion.OPEN
                                    showTypeChangeAlert = true
                                } else {
                                    draft.typeQuestion = TypeQuestion.OPEN
                                }
                            }
                        )
                    }
                }
            }
        }

        if (isEditing) {
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
        } else {
            Text(
                draft.questionText,
                fontFamily = jetbrainsMonoFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- 4. Alternatives Area ---
        if (draft.typeQuestion == TypeQuestion.ALTERNATIVE) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditing) {
                    draft.alternatives.forEachIndexed { index, alt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = alt.isCorrect == true,
                                onClick = {
                                    // Set only this one as correct
                                    val newAlts = draft.alternatives.map {
                                        it.copy(isCorrect = (it.id == alt.id))
                                    }
                                    draft.alternatives = newAlts
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(
                                        0xFF2E7D32
                                    )
                                )
                            )
                            // We can't edit text of Alternative DTO directly if it's not var in DTO.
                            // Since DTOs are vals, we replaced the list with copies.
                            // But TextField needs a way to update the string.
                            // We do a hacky immutable update:
                            OutlinedTextField(
                                value = alt.text,
                                onValueChange = { newText ->
                                    val newAlts = draft.alternatives.toMutableList()
                                    newAlts[index] = alt.copy(text = newText)
                                    draft.alternatives = newAlts
                                },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                    if (draft.alternatives.isEmpty()) {
                        Text(
                            "Añade alternativas (no implementado en backend para 'Crear' en este flow, solo editar existentes)",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Read Mode
                    draft.alternatives.forEach { alt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (alt.isCorrect == true) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (alt.isCorrect == true) Color(0xFF2E7D32) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alt.text,
                                fontFamily = jetbrainsMonoFamily,
                                fontSize = 14.sp,
                                color = if (alt.isCorrect == true) Color(0xFF2E7D32) else Color(
                                    0xFF131313
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "Campo de texto abierto",
                    color = Color.Gray,
                    fontFamily = jetbrainsMonoFamily
                )
            }
        }
    }
}