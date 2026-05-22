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
import kotlin.time.ExperimentalTime
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.project.components.ExerciseInfoSections
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.AlternativesDto
import org.example.project.dtos.CreateTestCompletedDto
import org.example.project.dtos.ExerciseContentDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.UpdateUserDto
import org.example.project.dtos.WordDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel

private const val TEST_PASS_THRESHOLD = 0.8f

class StudentTestResolverScreen(
    private val testId: Int,
    private val testName: String,
    private val unitId: Int,
    private val unitName: String,
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val exercisesVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo, unitId = unitId) }
        val userVm = rememberScreenModel {
            UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo)
        }

        val testUi by testVm.state.collectAsState()
        val unitUi by unitVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val studentName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarScope = rememberCoroutineScope()

        var submitted by remember { mutableStateOf(false) }
        var score by remember { mutableStateOf<Float?>(null) }
        var passing by remember { mutableStateOf(false) }
        val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
        val questionsByExercise = remember { mutableStateMapOf<Int, List<QuestionDto>>() }
        val alternativesByQuestion = remember { mutableStateMapOf<Int, List<AlternativesDto>>() }
        val contentByExercise = remember { mutableStateMapOf<Int, ExerciseContentDto?>() }
        val wordsByExercise = remember { mutableStateMapOf<Int, List<WordDto>>() }
        val vocabularyExpandedByExercise = remember { mutableStateMapOf<Int, Boolean>() }
        var loadingExerciseData by remember { mutableStateOf(false) }
        var resolverError by remember { mutableStateOf<String?>(null) }
        var selectedIndex by remember { mutableStateOf(1) }

        LaunchedEffect(testId) {
            testVm.getExercisesByTestId(testId)
            exercisesVm.searchExercises(FilterExercisesDto(unitId = unitId, isActive = true))
            if (userId != null && userId > 0) {
                testVm.getTestsCompletedByUser(userId)
                unitVm.getAllUnitsCompletedByUserId(userId)
            }
            if (userId != null && userId > 0) {
                userVm.getUserById(userId)
            }
        }

        suspend fun loadWordsForExercise(exerciseId: Int): List<WordDto> {
            val relations = RepositoryProvider.wordRepo.getExerciseWordsByExerciseId(exerciseId)
            return coroutineScope {
                relations.map { relation ->
                    async { RepositoryProvider.wordRepo.getWordById(relation.wordId) }
                }.awaitAll()
            }
        }

        LaunchedEffect(testUi.testExercises) {
            val exercises = testUi.testExercises.filter { it.isActive }.sortedBy { it.orderExercise }
            if (exercises.isEmpty()) {
                questionsByExercise.clear()
                alternativesByQuestion.clear()
                contentByExercise.clear()
                wordsByExercise.clear()
                loadingExerciseData = false
                resolverError = null
                return@LaunchedEffect
            }

            loadingExerciseData = true
            resolverError = null
            questionsByExercise.clear()
            alternativesByQuestion.clear()
            contentByExercise.clear()
            wordsByExercise.clear()

            try {
                coroutineScope {
                    exercises.map { exercise ->
                        async {
                            val exerciseId = exercise.id
                            contentByExercise[exerciseId] = runCatching {
                                RepositoryProvider.exerciseRepo.getExerciseContentByExerciseId(exerciseId)
                            }.getOrNull()

                            val questions = runCatching {
                                RepositoryProvider.questionRepo.getQuestionsByExerciseId(exerciseId)
                            }.getOrDefault(emptyList()).sortedBy { it.orderQuestion }

                            questionsByExercise[exerciseId] = questions

                            questions.forEach { question ->
                                val alternatives = runCatching {
                                    RepositoryProvider.questionRepo.getAlternativesByQuestionId(question.id)
                                }.getOrDefault(emptyList())

                                alternativesByQuestion[question.id] = alternatives
                                if (selectedAnswers[question.id] == null) {
                                    alternatives.firstOrNull()?.id?.let { selectedAnswers[question.id] = it }
                                }
                            }

                            wordsByExercise[exerciseId] = runCatching {
                                loadWordsForExercise(exerciseId)
                            }.getOrDefault(emptyList())
                        }
                    }.awaitAll()
                }
            } catch (e: Exception) {
                resolverError = "No se pudieron cargar los datos del test: ${e.message}"
            } finally {
                loadingExerciseData = false
            }
        }

        LaunchedEffect(testUi.error, userUi.error, resolverError) {
            val error = testUi.error ?: userUi.error ?: resolverError
            error?.let { snackbarHostState.showSnackbar(it) }
        }

        val orderedExercises = remember(testUi.testExercises) {
            testUi.testExercises.filter { it.isActive }.sortedBy { it.orderExercise }
        }
        val allQuestions = orderedExercises.flatMap { exercise ->
            questionsByExercise[exercise.id].orEmpty()
        }
        val completedUnitsCount = unitUi.unitsCompleted.size
        val totalActiveExercisesInUnit = remember(exercisesUi.exercises) {
            exercisesUi.exercises.count { it.unitId == unitId && it.isActive }
        }
        val latestAttempt = remember(testUi.testsCompleted, testId) {
            testVm.getLastAttemptForTest(testId)
        }

        var reviewStatus: org.example.project.dtos.UnitReviewStatusDto? by remember { mutableStateOf(null) }

        LaunchedEffect(userId, unitId, testId) {
            val safeUser = userId ?: return@LaunchedEffect
            val tId = testId ?: return@LaunchedEffect
            try {
                reviewStatus = RepositoryProvider.testRepo.getUnitReviewStatus(safeUser, unitId, tId)
            } catch (e: Exception) {
                println("Error cargando reviewStatus: ${e.message}")
            }
        }

        val remainingReviewExercises = reviewStatus?.remainingExercises ?: 0
        val reviewBlocked = latestAttempt?.score != 100 && remainingReviewExercises > 0

        fun submitAnswers() {
            val safeUserId = userId ?: return
            if (allQuestions.isEmpty()) return
            if (reviewBlocked) {
                // Informar al usuario por qué no puede enviar
                snackbarScope.launch {
                    snackbarHostState.showSnackbar("Debes aprobar $remainingReviewExercises ejercicio(s) de repaso antes de reintentar este test.")
                }
                return
            }

            val answered = allQuestions.count { selectedAnswers[it.id] != null }
            if (answered != allQuestions.size) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarScope.launch {
                    snackbarHostState.showSnackbar("Debes responder todas las preguntas antes de enviar las respuestas.")
                }
                return
            }

            val correct = allQuestions.count { question ->
                val selectedAlternativeId = selectedAnswers[question.id] ?: return@count false
                alternativesByQuestion[question.id]
                    .orEmpty()
                    .firstOrNull { it.id == selectedAlternativeId }
                    ?.isCorrect == true
            }

            val localScore = correct.toFloat() / allQuestions.size.toFloat()
            score = localScore
            passing = localScore >= TEST_PASS_THRESHOLD
            submitted = true

            testVm.createTestCompleted(
                CreateTestCompletedDto(
                    userId = safeUserId,
                    testId = testId,
                    score = (localScore * 100).toInt(),
                )
            )

            if (passing) {
                testVm.clearTestReviewRequirement(unitId)

                // Actualizar a la siguiente unidad si está disponible
                val currentUnitIndex = userUi.unit.indexOfFirst { it.id == unitId }
                if (currentUnitIndex >= 0 && currentUnitIndex + 1 < userUi.unit.size) {
                    val nextUnitId = userUi.unit[currentUnitIndex + 1].id ?: unitId
                    userVm.updateUser(
                        safeUserId,
                        UpdateUserDto(currentUnitId = nextUnitId)
                    )
                    UserSession.set(
                        id = safeUserId,
                        actualUnit = nextUnitId
                    )
                }
            } else {
                // No necesitamos marcar en frontend: el intento ya fue persistido en backend
                // El backend calculará que el usuario requiere repaso si aplica
            }
        }

        StudentAppLayout(
            actualScreen = testName,
            selectedIndex = 1,
            initialUserName = studentName,
            snackbarHostState = snackbarHostState,
            onSelect = { idx -> selectedIndex = idx },
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
                                    text = testName,
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
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
                                score?.let {
                                    if(it  >= 0f) {
                                        LinearProgressIndicator(
                                            progress = { (it ).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = if (passing) Color(0xFF2E7D32) else Color(0xFF1565C0)
                                        )
                                    }
                                }
                                if (submitted) {
                                    Text(
                                        text = if (passing) "¡Test aprobado! Acceso a la siguiente unidad desbloqueado." else "Test no aprobado. Intenta nuevamente.",
                                        color = if (passing) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (reviewBlocked) {
                                    Text(
                                        text = "Debes aprobar $remainingReviewExercises ejercicio(s) de repaso antes de reintentar este test.",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ){
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)){
                                Text(
                                    text = "⚠️ Resuelve el test con calma, debes repasar la unidad si fallas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8A5A00)
                                )

                            }
                        }
                    }

                    if (loadingExerciseData) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    } else if (allQuestions.isEmpty()) {
                        item {
                            ResolverEmptyCard()
                        }
                    } else {
                        items(orderedExercises, key = { it.id }) { exercise ->
                            val exerciseQuestions = questionsByExercise[exercise.id].orEmpty()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    ExerciseInfoSections(
                                        content = contentByExercise[exercise.id],
                                        words = wordsByExercise[exercise.id].orEmpty(),
                                        vocabularyExpanded = vocabularyExpandedByExercise[exercise.id] == true,
                                        onToggleVocabulary = {
                                            vocabularyExpandedByExercise[exercise.id] = !(vocabularyExpandedByExercise[exercise.id] ?: false)
                                        }
                                    )

                                    if (exerciseQuestions.isEmpty()) {
                                        Text(
                                            text = "Este ejercicio no tiene preguntas.",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        exerciseQuestions.forEach { question ->
                                            val alternatives = alternativesByQuestion[question.id].orEmpty()
                                            QuestionCard(
                                                question = question,
                                                alternatives = alternatives,
                                                selectedAlternativeId = selectedAnswers[question.id],
                                                onSelectAlternative = { selectedAnswers[question.id] = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { submitAnswers() },
                            enabled = !testUi.isLoading && allQuestions.isNotEmpty() && !reviewBlocked,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (testUi.isLoading) {
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

                    if (submitted && passing) {
                        item {
                            Button(
                                onClick = { navigator.pop() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text("Continuar a la siguiente unidad")
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
            Text("Este test no tiene preguntas", fontWeight = FontWeight.SemiBold)
            Text(
                "Agrega ejercicios al test para que aparezcan sus preguntas.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

