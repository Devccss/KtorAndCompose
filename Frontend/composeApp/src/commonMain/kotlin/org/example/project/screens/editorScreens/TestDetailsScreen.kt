package org.example.project.screens.editorScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.EditorLayout
import org.example.project.dtos.CreateTestExerciseDto
import org.example.project.dtos.CreateWelcomeTestDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateTestDto
import org.example.project.dtos.UpdateWelcomeTestDto
import org.example.project.dtos.WelcomeTestDto
import org.example.project.network.RepositoryProvider
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

class TestDetailsScreen(private val testId: Int) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val exercisesVm =
            rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val testUi by testVm.state.collectAsState()
        val exercisesUi by exercisesVm.state.collectAsState()

        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val unitUi by  unitVm.state.collectAsState()


        val snackbarHostState = remember { SnackbarHostState() }
        var selectedIndex by remember { mutableStateOf(4) } // Para mantener el estado de selección en el bottom bar

        // Cargar datos iniciales
        LaunchedEffect(testId) {
            testVm.getTestById(testId)
            testVm.getExercisesByTestId(testId)
            testVm.getAllWelcomeTests()
            exercisesVm.getAllExercises()
            unitVm.getUnitByTestId(testId)
        }

        LaunchedEffect(testUi.error) {
            testUi.error?.let { snackbarHostState.showSnackbar(it) }
        }

        EditorLayout(
            actualScreen = "Detalle del Test",
            snackbarHostState = snackbarHostState,
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
        ) { _, _, _ ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                val linkedExercises = exercisesUi.exercises.filter { ex ->
                    testUi.testExercises.any { it.id == ex.id }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // 1. Header del Test (Basado en ExercisesOrUnitScreen)
                        testUi.currentTest?.let { testData ->
                            TestHeader(
                                test = testData,
                                onEdit = { dto -> testVm.updateTest(testId, dto) },
                                unit = unitUi.actualUnit,
                                onDelete = {
                                    testVm.deleteTest(testId)
                                    navigator.pop()
                                }
                            )
                        }
                    }

                    item {
                        val welcomeRelation =
                            testUi.welcomeTests.firstOrNull { it.testId == testId }
                        WelcomeAssociationSection(
                            welcomeRelation = welcomeRelation,
                            isLoading = testUi.isLoading,
                            onAssociate = {
                                testVm.createWelcomeTest(
                                    CreateWelcomeTestDto(
                                        testId = testId,
                                        isActive = false
                                    )
                                )
                            },
                            onActivate = { testVm.activateWelcomeTest(testId) },
                            onDeactivate = {
                                testVm.updateWelcomeTest(
                                    testId,
                                    UpdateWelcomeTestDto(isActive = false)
                                )
                            },
                            onDelete = { testVm.deleteWelcomeTest(testId) }
                        )
                    }

                    item {
                        // 2. Sección de Ejercicios Vinculados
                        Text(
                            text = "Ejercicios del Test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    item {
                        AddExerciseToTestSection(
                            availableExercises = exercisesUi.exercises.filter { ex ->
                                testUi.testExercises.none { it.id == ex.id }
                            },
                            onAdd = { exerciseId ->
                                testVm.createTestExercise(
                                    CreateTestExerciseDto(
                                        testId = testId,
                                        exerciseId = exerciseId
                                    )
                                )
                            }
                        )
                    }

                    if (linkedExercises.isEmpty()) {
                        item {
                            Text(
                                "Este test no tiene ejercicios asignados.",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        itemsIndexed(
                            linkedExercises,
                            key = { _, exercise -> exercise.id }) { _, exercise ->
                            val relationId = testUi.testExercises.find { it.id == exercise.id }?.id

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ExerciseCard2(
                                        exercise = exercise,
                                        onClick = {
                                            navigator.push(
                                                ExercisesDetailsScreen(
                                                    exercise.id,
                                                    exercise.unitId
                                                )
                                            )
                                        }
                                    )
                                }

                                Box(Modifier.padding(horizontal = 8.dp)) {
                                    IconButton(
                                        onClick = { relationId?.let { testVm.deleteTestExercise(it) } },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Desvincular",
                                            tint = Color(0xFF8B0000)
                                        )
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
fun TestHeader(
    test: TestDto,
    onEdit: (UpdateTestDto) -> Unit,
    unit: UnitDto?,
    onDelete: () -> Unit
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(test.name) }
    var description by remember { mutableStateOf(test.description) }
    var isActive by remember { mutableStateOf(test.isActive) }

    LaunchedEffect(test) {
        if (!isEditing) {
            name = test.name
            description = test.description
            isActive = test.isActive
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "📊",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp, top = 4.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Título y Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            readOnly = !isEditing,
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = encodeSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF131313)
                            ),
                            modifier = if (isEditing) {
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            } else Modifier
                        )
                    }

                    if (!isEditing) {
                        Badge(
                            containerColor = if (isActive) Color(0xFFB8F4C4) else Color(0xFFFFD4D4),
                            contentColor = if (isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(if (isActive) "Activo" else "Borrador", fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(8.dp))



                    } else {
                        // Switch simple o texto clickable para activo
                        TextButton(onClick = { isActive = !isActive }) {
                            Text(
                                if (isActive) "Activo" else "Borrador",
                                color = if (isActive) Color(0xFF2D5E3D) else Color(0xFF8B0000)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))


                unit?.let {
                    Text(
                        "Unidad: ${unit.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }


                // Descripción
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    readOnly = !isEditing,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = jetbrainsMonoFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A),
                        lineHeight = 20.sp
                    ),
                    modifier = if (isEditing) {
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 120.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
            }

            // Botón Editar/Guardar
            Column(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(
                    onClick = {
                        if (isEditing) {
                            onEdit(
                                UpdateTestDto(
                                    name = name,
                                    description = description,
                                    isActive = isActive
                                )
                            )
                        }
                        isEditing = !isEditing
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isEditing) Color(0xFFB8F4C4) else Color(0xFFF0F0F0)
                        )
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Guardar" else "Editar",
                        tint = if (isEditing) Color(0xFF2D5E3D) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (isEditing){
                    IconButton(
                        onClick = {onDelete()},
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                shape = RoundedCornerShape(50.dp),
                                color = Color(0xFFFFD4D4)
                            )
                    ){
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = Color(0xFF8B0000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseToTestSection(
    availableExercises: List<ExerciseDto>,
    onAdd: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (!isExpanded) {
        Button(
            onClick = { isExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (!isExpanded) Color(0xFFB8F4C4) else Color.Gray ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF2D5E3D)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Vincular Ejercicios Existentes",
                color = Color(0xFF2D5E3D)
            )
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().border( 1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selecciona un ejercicio", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { isExpanded = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                //Advertencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.EventNote,
                        contentDescription = "Advertencia",
                        tint = Color.Gray,
                    )
                    Text("Los ejercicios vinculados a un test solo pueden ser resueltos dentro de el mismo, no estan disponibles dentro de una unidad.",
                        fontWeight = FontWeight.ExtraBold,
                        style =  MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )

                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nombre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                val filtered = availableExercises.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Text(
                        "No se encontraron ejercicios disponibles.",
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(filtered) { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAdd(exercise.id)
                                        // No cerramos inmediatamente para permitir agregar varios, o puedes poner isExpanded = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📝", modifier = Modifier.padding(end = 8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exercise.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        exercise.description ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    contentDescription = "Agregar",
                                    tint = Color(0xFF2D5E3D)
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeAssociationSection(
    welcomeRelation: WelcomeTestDto?,
    isLoading: Boolean,
    onAssociate: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Estado WelcomeTest", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            if (welcomeRelation == null) {
                Text("Este test no esta asociado como WelcomeTest.", color = Color.Gray)
                Button(
                    onClick = onAssociate,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8F4C4))
                ) {
                    Text("Asociar como WelcomeTest", color = Color(0xFF2D5E3D))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (welcomeRelation.isActive) "Welcome activo" else "Welcome inactivo",
                            fontWeight = FontWeight.SemiBold,
                            color = if (welcomeRelation.isActive) Color(0xFF2D5E3D) else Color(0xFF4A4A4A)
                        )
                        Text(
                            "Asociado: ${welcomeRelation.createAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Badge(
                        containerColor = if (welcomeRelation.isActive) Color(0xFFB8F4C4) else Color(0xFFE0E0E0),
                        contentColor = if (welcomeRelation.isActive) Color(0xFF2D5E3D) else Color(0xFF4A4A4A)
                    ) {
                        Text(if (welcomeRelation.isActive) "Activo" else "Inactivo")
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!welcomeRelation.isActive) {
                        Button(onClick = onActivate, enabled = !isLoading) {
                            Text("Seleccionar como activo")
                        }
                    } else {
                        OutlinedButton(onClick = onDeactivate, enabled = !isLoading) {
                            Text("Desactivar")
                        }
                    }

                    TextButton(onClick = onDelete, enabled = !isLoading) {
                        Text("Quitar asociacion", color = Color(0xFF8B0000))
                    }
                }
            }
        }
    }
}
