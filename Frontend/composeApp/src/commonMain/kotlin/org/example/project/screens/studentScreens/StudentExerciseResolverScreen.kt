package org.example.project.screens.studentScreens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.AlternativesDto
import org.example.project.dtos.CreateExerciseCompletedDto
import org.example.project.dtos.QuestionDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.QuestionViewModel

private const val EXERCISE_PASS_THRESHOLD = 0.8f

class StudentExerciseResolverScreen(
    private val exerciseId: Int,
    private val unitId: Int,
    private val unitName: String,
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val exerciseVm = rememberScreenModel {
            ExercisesViewModel(
                RepositoryProvider.exerciseRepo,
                unitId = unitId,
                exerciseId = exerciseId
            )
        }
        val questionVm = rememberScreenModel {
            QuestionViewModel(RepositoryProvider.questionRepo, exerciseId)
        }

        val exerciseUi by exerciseVm.state.collectAsState()
        val questionUi by questionVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }

        var submitted by remember { mutableStateOf(false) }
        var score by remember { mutableStateOf<Float?>(null) }
        var passing by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }
        val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

        LaunchedEffect(exerciseId) {
            exerciseVm.getExerciseById(exerciseId)
            questionVm.getQuestionsByExerciseId(exerciseId)
            if (userId != null && userId > 0) {
                exerciseVm.getExercisesCompletedByUserId(userId)
            }
        }

        LaunchedEffect(exerciseUi.error, questionUi.error) {
            val error = exerciseUi.error ?: questionUi.error
            error?.let { snackbarHostState.showSnackbar(it) }
        }

        val exercise = exerciseUi.selectedExercise
        val questions = questionUi.selectedQuestions.sortedBy { it.orderQuestion }
        val alternativesByQuestion = questionUi.alternatives
        val alreadyCompletedIds = remember(exerciseUi.completedExercises) {
            exerciseUi.completedExercises.map { it.exerciseId }.toSet()
        }

        LaunchedEffect(questions, alternativesByQuestion) {
            questions.forEach { question ->
                if (selectedAnswers[question.id] == null) {
                    alternativesByQuestion[question.id]?.firstOrNull()?.id?.let { firstAltId ->
                        selectedAnswers[question.id] = firstAltId
                    }
                }
            }
        }

        fun submitAnswers() {
            val safeUserId = userId ?: return
            if (questions.isEmpty()) return

            val answered = questions.count { selectedAnswers[it.id] != null }
            if (answered != questions.size) {
                snackbarHostState.currentSnackbarData?.dismiss()
                return
            }

            val correct = questions.count { question ->
                val selectedAlternativeId = selectedAnswers[question.id] ?: return@count false
                alternativesByQuestion[question.id]
                    .orEmpty()
                    .firstOrNull { it.id == selectedAlternativeId }
                    ?.isCorrect == true
            }

            val localScore = correct.toFloat() / questions.size.toFloat()
            score = localScore
            passing = localScore >= EXERCISE_PASS_THRESHOLD
            submitted = true

            if (passing && exerciseId !in alreadyCompletedIds) {
                isSaving = true
                val now = Clock.System.now().toString()
                exerciseVm.createExerciseCompleted(
                    CreateExerciseCompletedDto(
                        userId = safeUserId,
                        exerciseId = exerciseId,
                        completedAt = now
                    )
                )
                isSaving = false
            }
        }

        StudentAppLayout(
            actualScreen = exercise?.name ?: "Resolver ejercicio",
            selectedIndex = 1,
            initialUserName = studentName,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 18.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise?.name ?: "Ejercicio",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = unitName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8FF))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (submitted) {
                                        "Resultado: ${(score ?: 0f) * 100f}%"
                                    } else {
                                        "Resuelve las preguntas y envía tus respuestas"
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                                LinearProgressIndicator(
                                    progress = { (score ?: 0f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (submitted) {
                                    Text(
                                        text = if (passing) "Ejercicio aprobado" else "Ejercicio no aprobado",
                                        color = if (passing) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (questions.isEmpty()) {
                        item {
                            ResolverEmptyCard()
                        }
                    } else {
                        items(questions, key = { it.id }) { question ->
                            val alternatives = alternativesByQuestion[question.id].orEmpty()
                            QuestionCard(
                                question = question,
                                alternatives = alternatives,
                                selectedAlternativeId = selectedAnswers[question.id],
                                onSelectAlternative = { selectedAnswers[question.id] = it }
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { submitAnswers() },
                            enabled = !isSaving && questions.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(if (submitted) "Volver a evaluar" else "Enviar respuestas")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionDto,
    alternatives: List<AlternativesDto>,
    selectedAlternativeId: Int?,
    onSelectAlternative: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFF003AB6))
                Text(question.questionText, fontWeight = FontWeight.SemiBold)
            }

            if (alternatives.isEmpty()) {
                Text(
                    text = "Sin alternativas disponibles",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                alternatives.forEach { alternative ->
                    AlternativeRow(
                        text = alternative.text,
                        selected = selectedAlternativeId == alternative.id,
                        onClick = { onSelectAlternative(alternative.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlternativeRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (selected) Color(0xFF1565C0) else Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ResolverEmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Este ejercicio no tiene preguntas", fontWeight = FontWeight.SemiBold)
            Text(
                "Agrega preguntas y alternativas desde el panel de administración.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
