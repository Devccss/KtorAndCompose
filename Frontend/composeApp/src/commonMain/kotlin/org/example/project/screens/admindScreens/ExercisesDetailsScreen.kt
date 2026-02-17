package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.AlternativesDto // Importar DTO
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.TypeQuestion
import org.example.project.dtos.UpdateAlternativeDto
import org.example.project.dtos.UpdateQuestionDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.QuestionViewModel
import org.jetbrains.compose.resources.Font

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

        val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
        val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

        LaunchedEffect(exerciseUi.error) {
            exerciseUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
            questionUi.error?.let {
                snackbarHostState.showSnackbar(it)
                println( "Error en QuestionViewModel: ${questionUi.error}")
            }
        }

        AppLayout(
            actualScreen = "Detalles del Ejercicio",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8F0))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (exerciseUi.isLoading || questionUi.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF003AB6))
                    }
                } else {
                    // Header del Ejercicio (Titulo, Descripcion, Botones de acción)
                    exerciseUi.selectedExercise?.let { exercise ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = exercise.name,
                                    fontFamily = encodeSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF131313)
                                )
                                // Badge de estado
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (exercise.isActive) Color(0xFFE6F4EA) else Color(
                                                0xFFFFF4E5
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (exercise.isActive) "Activo" else "Inactivo",
                                        color = if (exercise.isActive) Color(0xFF1E7E34) else Color(
                                            0xFFB95000
                                        ),
                                        fontSize = 12.sp,
                                        fontFamily = jetbrainsMonoFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (!exercise.description.isNullOrBlank()) {
                                Text(
                                    text = exercise.description,
                                    fontFamily = jetbrainsMonoFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botones globales del ejercicio
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { /* Abrir dialogo editar ejercicio */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF003AB6
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Editar Info", fontFamily = jetbrainsMonoFamily)
                                }

                                OutlinedButton(
                                    onClick = { /* Confirmar eliminar */ },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(
                                            0xFFD32F2F
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFFD32F2F).copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar", fontFamily = jetbrainsMonoFamily)
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Gray.copy(alpha = 0.2f)
                        )

                        // Lista de tarjetas de preguntas
                        val questions = questionUi.selectedQuestions
                        if (questions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay preguntas en este ejercicio.",
                                    fontFamily = jetbrainsMonoFamily,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            questions.forEach { question ->
                                // Obtenemos las alternativas especificas para esta pregunta desde el mapa
                                val specificAlternatives =
                                    questionUi.alternatives[question.id] ?: emptyList()

                                QuestionDetailCard(
                                    question = question,
                                    alternatives = specificAlternatives,
                                    onSave = { updatedQuestion, updatedAlternatives ->
                                        questionVm.updateQuestion(question.id,updatedQuestion)
                                        updatedAlternatives.forEach { alternative ->
                                            questionVm.updateAlternativesForQuestion(alternative.id,
                                                UpdateAlternativeDto(
                                                    text = alternative.text,
                                                    isCorrect = alternative.isCorrect
                                                )
                                            )
                                        }
                                        

                                        println("Pregunta actualizada: $updatedQuestion")
                                        println("Alternativas actualizadas: $updatedAlternatives")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionDetailCard(
    question: QuestionDto,
    alternatives: List<AlternativesDto>,
    onSave: (UpdateQuestionDto, List<AlternativesDto>) -> Unit = { _, _ -> }
) { // Recibir alternativas
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

    // Estado para modo edición
    var isEditing by remember { mutableStateOf(false) }

    // Estados para los campos editables
    var textContent by remember { mutableStateOf(question.textContent) }
    var grammarExplanation by remember { mutableStateOf(question.grammarExplanation) }
    var questionText by remember { mutableStateOf(question.questionText) }
    var selectedAlt by remember { mutableStateOf<Int?>(null) } // Para preguntas de alternativa, almacenar la alternativa seleccionada (por ejemplo, por ID)

    // Inicializar la alternativa seleccionada correctamente al entrar en edición
    LaunchedEffect(alternatives) {
        if (selectedAlt == null) {
            selectedAlt = alternatives.find { it.isCorrect == true }?.id
        }
    }

    // Estado local para alternativas (para edición)
    // Nota: Esto es simplificado, idealmente tendrías una lista mutable de DTOs
    // var localAlternatives by remember(alternatives) { mutableStateOf(alternatives) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SECCIÓN 1: Explicación y Contexto ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Explicación / Contexto",
                        fontFamily = encodeSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF003AB6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = jetbrainsMonoFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF131313)
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF003AB6),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Text(
                            text = textContent,
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF131313),
                            lineHeight = 20.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isEditing) {
                        // Botón Cancelar
                        IconButton(
                            onClick = {
                                isEditing = false
                                // Reset values
                                textContent = question.textContent
                                grammarExplanation = question.grammarExplanation
                                questionText = question.questionText
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Botón Guardar
                        IconButton(
                            onClick = {
                                isEditing = false
                                // Preparamos la pregunta actualizada
                                val updatedQuestion = question.copy(
                                    textContent = textContent,
                                    grammarExplanation = grammarExplanation,
                                    questionText = questionText
                                )

                                // Preparamos las alternativas actualizadas (marcando isCorrect según la selección)
                                val updatedAlternatives = if (question.typeQuestion == TypeQuestion.ALTERNATIVE) {
                                    alternatives.map { alt ->
                                        alt.copy(isCorrect = (alt.id == selectedAlt))
                                    }
                                } else {
                                    alternatives
                                }

                                // Enviamos los datos al callback
                                onSave(
                                    UpdateQuestionDto(
                                    textContent = updatedQuestion.textContent,
                                    grammarExplanation = updatedQuestion.grammarExplanation,
                                    questionText = updatedQuestion.questionText,
                                ), updatedAlternatives)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Guardar",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Botón Editar
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF0F4FF), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Default.Create,
                                contentDescription = "Editar Pregunta",
                                tint = Color(0xFF003AB6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // --- SECCIÓN 2: Gramática ---
            Column {
                Text(
                    text = "Gramática",
                    fontFamily = encodeSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF6C757D)
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = grammarExplanation,
                        onValueChange = { grammarExplanation = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 13.sp
                        ),
                        placeholder = { Text("Explicación gramatical...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF003AB6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = grammarExplanation.ifBlank { "Sin explicación gramatical" },
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF495057),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // --- SECCIÓN 3: Pregunta y Alternativas ---
            Column {
                Text(
                    text = "Pregunta",
                    fontFamily = encodeSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF131313)
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = jetbrainsMonoFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF003AB6),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Text(
                        text = questionText,
                        fontFamily = jetbrainsMonoFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF131313)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visualización según tipo
                if (question.typeQuestion == TypeQuestion.ALTERNATIVE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEditing) {
                            alternatives.forEach { alternative ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (alternative.id == selectedAlt),
                                        onClick = { selectedAlt = alternative.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF2E7D32),
                                            unselectedColor = Color.Gray.copy(alpha = 0.5f)
                                        ), modifier = Modifier.padding(2.dp)
                                    )
                                    (Text(
                                        text = alternative.text,
                                        fontFamily = jetbrainsMonoFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF131313)
                                    ))
                                }
                            }
                        } else {
                            // Mostrar las alternativas reales
                            if (alternatives.isEmpty()) {
                                Text(
                                    "Cargando alternativas o vacía...",
                                    fontFamily = jetbrainsMonoFamily,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            } else {
                                alternatives.forEach { alt ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (alt.isCorrect == true) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (alt.isCorrect == true) Color(0xFF2E7D32) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = alt.text, // Asumiendo que AlternativeDto tiene 'text'
                                            fontFamily = jetbrainsMonoFamily,
                                            fontSize = 14.sp,
                                            color = if (alt.isCorrect == true) Color(0xFF2E7D32) else Color(
                                                0xFF131313
                                            ) // Color normal para texto de incorrectas
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.TextFields,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Campo de texto abierto",
                                fontFamily = jetbrainsMonoFamily,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}