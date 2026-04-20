package org.example.project.screens.studentScreens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.project.components.ExerciseInfoSections
import org.example.project.components.StudentAppLayout
import org.example.project.dtos.CreateExerciseCompletedDto
import org.example.project.dtos.CreateTestCompletedDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.ExerciseContentDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.QuestionDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.WordDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.example.project.viewModel.UserViewModel
import kotlin.time.ExperimentalTime

private const val WELCOME_EXERCISE_PASS_THRESHOLD = 0.8f
private const val UNIT_DOMINANCE_EFFECTIVE_SCORE = 3
private const val SPARSE_UNIT_EXERCISE_THRESHOLD = 3

class StudentWelcomeScreen(val id: Int? = null) : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val exercisesVm =
            rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val unitVm =
            rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo, autoLoad = false) }
        val userVm = rememberScreenModel {
            UserViewModel(
                RepositoryProvider.userRepo,
                RepositoryProvider.unitRepo
            )
        }

        val testUi by testVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()
        val unitUi by unitVm.state.collectAsState()
        val userUi by userVm.state.collectAsState()

        val userId = id ?: UserSession.idUser
        val name = UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(userId) {
            testVm.getAllWelcomeTests()
            exercisesVm.searchExercises(FilterExercisesDto(isActive = true))
            unitVm.searchUnits(FilterUnitsDto(isActive = true))

            if (userId != null && userId > 0) {
                testVm.getTestsCompletedByUser(userId)
                exercisesVm.getExercisesCompletedByUserId(userId)
                userVm.getUserById(userId)
            }
        }

        LaunchedEffect(userUi.currentUser) {
            userUi.currentUser.let {
                if (it?.id != null) {
                    UserSession.set(
                        id = it.id,
                        name = it.name,
                        role = it.role,
                        actualUnit = it.currentUnitId
                    )
                }
            }
        }

        val activeWelcome = testUi.welcomeTests.firstOrNull { it.isActive }

        LaunchedEffect(activeWelcome?.testId) {
            activeWelcome?.testId?.let { testVm.getExercisesByTestId(it) }
        }

        val unitsOrdered =
            remember(unitUi.units) { unitUi.units.filter { it.isActive }.sortedBy { it.orderUnit } }
        val progress =
            remember(unitsOrdered, exercisesUi.exercises, exercisesUi.completedExercises) {
                estimatePlacement(
                    units = unitsOrdered,
                    allExercises = exercisesUi.exercises.filter { it.isActive },
                    completedExerciseIds = exercisesUi.completedExercises.map { it.exerciseId }
                        .toSet()
                )
            }

        // Evitamos recalcular/sobrescribir la unidad actual en Inicio.
        // La unidad debe actualizarse solo al completar Welcome Test o al aprobar flujos de aprendizaje.

        LaunchedEffect(testUi.error, exercisesUi.error, userUi.error) {
            val error = testUi.error ?: exercisesUi.error ?: userUi.error
            error?.let {
                println("Error detectado en StudentWelcomeScreen: $it")
                snackbarHostState.showSnackbar(it)
            }
        }

        val completedWelcomeTestIds = testUi.testsCompleted.map { it.testId }.toSet()
        val activeWelcomePending =
            activeWelcome?.testId?.let { it !in completedWelcomeTestIds } == true
        val currentUnitName = unitsOrdered.firstOrNull {
            it.id == (UserSession.actualUnit ?: progress.currentUnitId)
        }?.name

        StudentAppLayout(
            actualScreen = "Inicio",
            selectedIndex = 0,
            initialUserName = name,
            snackbarHostState = snackbarHostState,
            onAvatarClick = {
                navigator.push(
                    StudentMeUserScreen(
                        userIdArg = userId,
                        studentNameArg = name
                    )
                )
            }
        ) { _, _, _ ->
            StudentDashboardContent(
                name = name,
                currentUnitName = currentUnitName,
                progress = progress,
                welcomeTestPending = activeWelcomePending,
                welcomeExercises = testUi.testExercises.filter { it.isActive },
                isLoading = testUi.isLoading || exercisesUi.isLoading || unitUi.isLoading,
                units = unitsOrdered,
                onFinishWelcome = { resolvedIds, placement ->
                    val safeUserId = userId ?: return@StudentDashboardContent
                    val safeTestId = activeWelcome?.testId ?: return@StudentDashboardContent

                    val alreadyCompleted =
                        exercisesUi.completedExercises.map { it.exerciseId }.toSet()
                    val newResolved = resolvedIds.filterNot { it in alreadyCompleted }

                    newResolved.forEach { exerciseId ->
                        exercisesVm.createExerciseCompleted(
                            CreateExerciseCompletedDto(
                                userId = safeUserId,
                                exerciseId = exerciseId
                            )
                        )
                    }

                    testVm.createTestCompleted(
                        CreateTestCompletedDto(
                            userId = safeUserId,
                            testId = safeTestId,
                            score = placement.progressPercent,
                        )
                    )

                    val targetUnitId = placement.currentUnitId
                    if (targetUnitId != null && targetUnitId != UserSession.actualUnit) {
                        userVm.updateUserCurrentUnit(safeUserId, targetUnitId)
                        UserSession.set(
                            id = safeUserId,
                            name = UserSession.name,
                            role = UserSession.role,
                            actualUnit = targetUnitId
                        )
                    }

                    testVm.getTestsCompletedByUser(safeUserId)
                    exercisesVm.getExercisesCompletedByUserId(safeUserId)
                }
            )
        }
    }
}

@Composable
fun StudentDashboardContent(
    name: String,
    currentUnitName: String?,
    progress: PlacementSummary,
    welcomeTestPending: Boolean,
    welcomeExercises: List<ExerciseDto>,
    isLoading: Boolean,
    units: List<UnitDto>,
    onFinishWelcome: (Set<Int>, PlacementSummary) -> Unit,
) {
    var expandedWelcome by remember { mutableStateOf(welcomeTestPending) }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hola, $name",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131313)
                    )
                    Text(
                        text = if (welcomeTestPending) {
                            "Antes de continuar, realiza el Welcome Test para ubicarte en la unidad correcta."
                        } else {
                            "Tu unidad actual: ${currentUnitName ?: "Sin asignar"}."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B6B6B)
                    )
                }

            }

            if (welcomeTestPending) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF003AB6), Color(0xFF48145B))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "WELCOME TEST",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Test de nivelacion pendiente",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = {
                                            (progress.progressPercent / 100f).coerceIn(
                                                0f,
                                                1f
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(0.8f).height(6.dp),
                                        color = Color(0xFFB8F4C4),
                                        trackColor = Color.White.copy(alpha = 0.3f),
                                        strokeCap = StrokeCap.Round,
                                    )
                                }
                                IconButton(
                                    onClick = { expandedWelcome = !expandedWelcome },
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.White, RoundedCornerShape(50)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Accion principal",
                                        tint = Color(0xFF003AB6),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Tu progreso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131313)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Completado",
                        value = "${progress.progressPercent}%",
                        color = Color(0xFFE0F2F1),
                        textColor = Color(0xFF00695C),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Unidades dominadas",
                        value = progress.dominatedUnits.toString(),
                        color = Color(0xFFFFF3E0),
                        textColor = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (welcomeTestPending) {
                item {
                    WelcomePlacementCard(
                        expanded = expandedWelcome,
                        isLoading = isLoading,
                        welcomeExercises = welcomeExercises,
                        units = units,
                        onToggleExpand = { expandedWelcome = !expandedWelcome },
                        onFinish = { passedExercises, placement ->
                            onFinishWelcome(passedExercises, placement)
                            expandedWelcome = false
                        }
                    )
                }
            }

            if (progress.bonusPointsFromSparseUnits > 0) {
                item {
                    InfoCard(
                        icon = Icons.Default.School,
                        title = "Estimacion aplicada",
                        description = "Se aplicaron ${progress.bonusPointsFromSparseUnits} punto(s) extra por unidades con pocos ejercicios."
                    )
                }
            }
        }
    }

}

@Composable
private fun WelcomePlacementCard(
    expanded: Boolean,
    isLoading: Boolean,
    welcomeExercises: List<ExerciseDto>,
    units: List<UnitDto>,
    onToggleExpand: () -> Unit,
    onFinish: (Set<Int>, PlacementSummary) -> Unit,
) {
    val questionRepo = RepositoryProvider.questionRepo
    val exerciseRepo = RepositoryProvider.exerciseRepo
    val wordRepo = RepositoryProvider.wordRepo
    val questionsByExercise =
        remember(welcomeExercises) { mutableStateMapOf<Int, List<QuestionDto>>() }
    val alternativesByQuestion =
        remember(welcomeExercises) { mutableStateMapOf<Int, List<org.example.project.dtos.AlternativesDto>>() }
    val contentByExercise =
        remember(welcomeExercises) { mutableStateMapOf<Int, ExerciseContentDto?>() }
    val wordsByExercise = remember(welcomeExercises) { mutableStateMapOf<Int, List<WordDto>>() }
    val vocabularyExpandedByExercise =
        remember(welcomeExercises) { mutableStateMapOf<Int, Boolean>() }
    val selectedAnswers = remember(welcomeExercises) { mutableStateMapOf<Int, Int>() }

    var loadingResolver by remember(welcomeExercises) { mutableStateOf(false) }
    var resolverError by remember(welcomeExercises) { mutableStateOf<String?>(null) }
    var submitted by remember(welcomeExercises) { mutableStateOf(false) }
    var passedExerciseIds by remember(welcomeExercises) { mutableStateOf(emptySet<Int>()) }
    var placementResult by remember(welcomeExercises) { mutableStateOf<PlacementSummary?>(null) }

    suspend fun loadWordsForExercise(exerciseId: Int): List<WordDto> {
        val relations = wordRepo.getExerciseWordsByExerciseId(exerciseId)
        return coroutineScope {
            relations.map { relation ->
                async { wordRepo.getWordById(relation.wordId) }
            }.awaitAll()
        }
    }

    LaunchedEffect(expanded, welcomeExercises) {
        if (!expanded || welcomeExercises.isEmpty() || questionsByExercise.isNotEmpty()) return@LaunchedEffect
        loadingResolver = true
        resolverError = null
        try {
            coroutineScope {
                welcomeExercises.sortedBy { it.orderExercise }.map { exercise ->
                    async {
                        val exerciseId = exercise.id
                        contentByExercise[exerciseId] = runCatching {
                            exerciseRepo.getExerciseContentByExerciseId(exerciseId)
                        }.getOrNull()

                        val questions = questionRepo.getQuestionsByExerciseId(exerciseId)
                            .sortedBy { it.orderQuestion }
                        questionsByExercise[exerciseId] = questions

                        questions.forEach { question ->
                            val alternatives = questionRepo.getAlternativesByQuestionId(question.id)
                            alternativesByQuestion[question.id] = alternatives
                            if (selectedAnswers[question.id] == null) {
                                alternatives.firstOrNull()?.id?.let { firstAlternativeId ->
                                    selectedAnswers[question.id] = firstAlternativeId
                                }
                            }
                        }

                        wordsByExercise[exerciseId] = runCatching {
                            loadWordsForExercise(exerciseId)
                        }.getOrDefault(emptyList())
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            resolverError = "No se pudo cargar el welcome test: ${e.message}"
        } finally {
            loadingResolver = false
        }
    }

    fun evaluateWelcomeTest() {
        val allQuestions = questionsByExercise.values.flatten()
        if (allQuestions.isEmpty()) {
            resolverError = "Este welcome test no tiene preguntas para evaluar."
            return
        }

        val hasUnanswered = allQuestions.any { selectedAnswers[it.id] == null }
        if (hasUnanswered) {
            resolverError = "Responde todas las preguntas antes de finalizar."
            return
        }

        val passed = mutableSetOf<Int>()
        welcomeExercises.forEach { exercise ->
            val questions = questionsByExercise[exercise.id].orEmpty()
            if (questions.isEmpty()) return@forEach

            val correctCount = questions.count { question ->
                val selectedAlternativeId = selectedAnswers[question.id] ?: return@count false
                alternativesByQuestion[question.id]
                    .orEmpty()
                    .firstOrNull { it.id == selectedAlternativeId }
                    ?.isCorrect == true
            }

            val score = correctCount.toFloat() / questions.size.toFloat()
            if (score >= WELCOME_EXERCISE_PASS_THRESHOLD) {
                passed += exercise.id
            }
        }

        val placement = estimatePlacement(
            units = units,
            allExercises = welcomeExercises,
            completedExerciseIds = passed
        )

        passedExerciseIds = passed
        placementResult = placement
        resolverError = null
        submitted = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Realizar Welcome Test", fontWeight = FontWeight.Bold)
                    Text(
                        "Resuelve preguntas reales. Se aprueba cada ejercicio con 80% o mas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                TextButton(onClick = onToggleExpand) {
                    Text(if (expanded) "Ocultar" else "Mostrar")
                }
            }

            if (expanded) {
                if (isLoading || loadingResolver) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (welcomeExercises.isEmpty()) {
                    Text("Este welcome test aun no tiene ejercicios.", color = Color.Gray)
                } else {
                    welcomeExercises.sortedBy { it.orderExercise }.forEach { exercise ->
                        val questions =
                            questionsByExercise[exercise.id].orEmpty().sortedBy { it.orderQuestion }
                        val unitName = units.firstOrNull { it.id == exercise.unitId }?.name
                            ?: "Unidad ${exercise.unitId}"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE7EDF3), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    unitName,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                ExerciseInfoSections(
                                    content = contentByExercise[exercise.id],
                                    words = wordsByExercise[exercise.id].orEmpty(),
                                    vocabularyExpanded = vocabularyExpandedByExercise[exercise.id] == true,
                                    onToggleVocabulary = {
                                        vocabularyExpandedByExercise[exercise.id] =
                                            !(vocabularyExpandedByExercise[exercise.id] ?: false)
                                    }
                                )

                                if (questions.isEmpty()) {
                                    Text(
                                        text = "Este ejercicio no tiene preguntas.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    questions.forEach { question ->
                                        val alternatives =
                                            alternativesByQuestion[question.id].orEmpty()
                                        WelcomeQuestionCard(
                                            question = question,
                                            alternatives = alternatives,
                                            selectedAlternativeId = selectedAnswers[question.id],
                                            onSelectAlternative = {
                                                selectedAnswers[question.id] = it
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                resolverError?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                placementResult?.let { result ->
                    val unitName =
                        units.firstOrNull { it.id == result.currentUnitId }?.name ?: "Sin unidad"
                    InfoCard(
                        icon = Icons.Default.Flag,
                        title = "Resultado del Welcome Test",
                        description = "Quedaste en $unitName. Ejercicios aprobados: ${passedExerciseIds.size}/${welcomeExercises.size}."
                    )
                }

                Button(
                    onClick = { evaluateWelcomeTest() },
                    enabled = !isLoading && !loadingResolver && welcomeExercises.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (submitted) "Recalcular resultado" else "Evaluar Welcome Test")
                }

                Button(
                    onClick = {
                        val placement = placementResult ?: estimatePlacement(
                            units = units,
                            allExercises = welcomeExercises,
                            completedExerciseIds = passedExerciseIds
                        )
                        onFinish(passedExerciseIds, placement)
                    },
                    enabled = !isLoading && submitted,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar resultado y continuar")
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF003AB6))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class PlacementSummary(
    val currentUnitId: Int?,
    val dominatedUnits: Int,
    val completedExercises: Int,
    val totalExercises: Int,
    val progressPercent: Int,
    val bonusPointsFromSparseUnits: Int,
)

private fun estimatePlacement(
    units: List<UnitDto>,
    allExercises: List<ExerciseDto>,
    completedExerciseIds: Set<Int>,
): PlacementSummary {
    if (units.isEmpty()) {
        return PlacementSummary(
            currentUnitId = null,
            dominatedUnits = 0,
            completedExercises = completedExerciseIds.size,
            totalExercises = allExercises.size,
            progressPercent = 0,
            bonusPointsFromSparseUnits = 0
        )
    }

    val exercisesByUnit = allExercises.groupBy { it.unitId }
    val completedByUnit = allExercises
        .filter { it.id in completedExerciseIds }
        .groupingBy { it.unitId }
        .eachCount()

    val orderedUnits = units.sortedBy { it.orderUnit }
    val effectiveScoreByUnit = orderedUnits
        .mapNotNull { it.id }
        .associateWith { 0 }
        .toMutableMap()

    var bonusFromSparse = 0

    orderedUnits.forEachIndexed { index, unit ->
        val unitId = unit.id ?: return@forEachIndexed
        val totalInUnit = exercisesByUnit[unitId]?.size ?: 0
        val completedInUnit = completedByUnit[unitId] ?: 0
        if (totalInUnit == 0 || completedInUnit == 0) return@forEachIndexed

        val isSparse = totalInUnit < SPARSE_UNIT_EXERCISE_THRESHOLD
        if (isSparse) {
            val transferPoints = completedInUnit * 2
            val previousUnitId = orderedUnits.getOrNull(index - 1)?.id
            val targetUnitId = previousUnitId ?: unitId

            effectiveScoreByUnit[targetUnitId] =
                (effectiveScoreByUnit[targetUnitId] ?: 0) + transferPoints
            bonusFromSparse += transferPoints
        } else {
            effectiveScoreByUnit[unitId] = (effectiveScoreByUnit[unitId] ?: 0) + completedInUnit
        }
    }

    // Regla progresiva: dominar una unidad implica dominar todas las anteriores.
    val highestDominatedIndex = orderedUnits.indexOfLast { unit ->
        val unitId = unit.id ?: return@indexOfLast false
        (effectiveScoreByUnit[unitId] ?: 0) >= UNIT_DOMINANCE_EFFECTIVE_SCORE
    }

    val dominated = (highestDominatedIndex + 1).coerceAtLeast(0)
    val currentUnitId = when {
        orderedUnits.isEmpty() -> null
        highestDominatedIndex < 0 -> orderedUnits.firstOrNull()?.id
        else -> orderedUnits.getOrNull(highestDominatedIndex)?.id
    }

    val totalExercises = allExercises.size
    val completedExercises = allExercises.count { it.id in completedExerciseIds }
    val percent = if (totalExercises > 0) {
        ((completedExercises.toFloat() / totalExercises.toFloat()) * 100f).toInt()
    } else 0

    return PlacementSummary(
        currentUnitId = currentUnitId,
        dominatedUnits = dominated,
        completedExercises = completedExercises,
        totalExercises = totalExercises,
        progressPercent = percent.coerceIn(0, 100),
        bonusPointsFromSparseUnits = bonusFromSparse
    )
}

@Composable
private fun WelcomeQuestionCard(
    question: QuestionDto,
    alternatives: List<org.example.project.dtos.AlternativesDto>,
    selectedAlternativeId: Int?,
    onSelectAlternative: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(question.questionText, fontWeight = FontWeight.Medium)

        if (alternatives.isEmpty()) {
            Text(
                text = "Sin alternativas disponibles",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            alternatives.forEach { alternative ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (selectedAlternativeId == alternative.id) Color(0xFF1565C0) else Color(
                                0xFFE0E0E0
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectAlternative(alternative.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = selectedAlternativeId == alternative.id,
                        onClick = { onSelectAlternative(alternative.id) }
                    )
                    Text(alternative.text, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

