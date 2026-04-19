package org.example.project.screens.admindScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import org.example.project.components.AppLayout
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterTestsDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.WelcomeTestDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

class TestScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val testUi by testVm.state.collectAsState()

        val exercisesVm =
            rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo) }
        val exercisesUi by exercisesVm.state.collectAsState()

        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val unitUi by unitVm.state.collectAsState()


        var selectedIndex by remember { mutableStateOf(4) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(testUi.error) {
            testUi.error?.let {
                snackbarHostState.showSnackbar(it)
                println("Error en TestScreen: $it")
            }
        }
        LaunchedEffect(Unit) {
            testVm.getAllTests()
            testVm.getAllWelcomeTests()
            testVm.getAllTestsFromWelcomeTests()
            exercisesVm.getAllExercises()
        }


        AppLayout(
            actualScreen = "Administrar Tests",
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                TestSection(
                    welcomeT = testUi.selectedTests,
                    allTests = testUi.allTests, // Pasamos la lista de tests como datos reales
                    allUnits = unitUi.units,
                    welcomeTests = testUi.welcomeTests,
                    allExercises = exercisesUi.exercises,
                    isLoading = testUi.isLoading,
                    onCreateTest = { testVm.createTest(it) },
                    onSetWelcome = { testVm.setWelcomeTest(it) },
                    onActivateWelcome = { testVm.activateWelcomeTest(it) },
                    onCreateWelcome = { dto, exerciseIds ->
                        testVm.createAndSetWelcomeTest(
                            dto,
                            exerciseIds
                        )
                    },
                    onDeleteWelcome = { testVm.deleteWelcomeTest(it) },
                    onNavigate = { navigator.push(it) },
                    onFilter = { testVm.searchTests(it) },
                    onError = { testVm.updateMessage(it.message) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSection(
    welcomeT: List<TestDto>,
    allTests: List<TestDto>, // Lista origen datos reales
    allUnits: List<UnitDto> = emptyList(), // Lista de unidades para el dropdown
    welcomeTests: List<WelcomeTestDto>,
    allExercises: List<ExerciseDto>,
    isLoading: Boolean,
    onCreateTest: (CreateTestDto) -> Unit,
    onSetWelcome: (Int) -> Unit,
    onActivateWelcome: (Int) -> Unit,
    onCreateWelcome: (CreateTestDto, List<Int>) -> Unit,
    onDeleteWelcome: (Int) -> Unit,
    onFilter: (FilterTestsDto) -> Unit,
    onNavigate: (Screen) -> Unit = {},
    onError: (Error) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isFiltering by remember { mutableStateOf(false) }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    var filterActiveExpanded by remember { mutableStateOf(false) }

    var isAddingTest by remember { mutableStateOf(false) }
    var nameTest by remember { mutableStateOf("") }
    var descriptionTest by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<UnitDto?>(null) }
    var unitMenu by remember { mutableStateOf(false) }

    var welcomeMode by remember { mutableStateOf<WelcomeMode>(WelcomeMode.NONE) }
    var existingWelcomeMenu by remember { mutableStateOf(false) }
    var selectedExistingWelcomeTest by remember { mutableStateOf<TestDto?>(null) }
    var newWelcomeName by remember { mutableStateOf("") }
    var newWelcomeDescription by remember { mutableStateOf("") }
    var searchWelcomeExercise by remember { mutableStateOf("") }
    var selectedWelcomeExercises by remember { mutableStateOf(setOf<Int>()) }

    val activeWelcome = welcomeTests.firstOrNull { it.isActive }
    val associatedWelcomeIds = welcomeTests.map { it.testId }.toSet()
    val existingWelcomeCandidates = allTests.filter { it.id !in associatedWelcomeIds }
    val activeWelcomeTestId = activeWelcome?.testId
    val defaultUnitIdForWelcome = allUnits.firstOrNull()?.id
    val exercisesForNewWelcome = allExercises
        .filter { exercise -> exercise.name.contains(searchWelcomeExercise, ignoreCase = true) }

    LaunchedEffect(searchQuery, filterActive) {
        onFilter(
            FilterTestsDto(
                name = searchQuery,
                isActive = filterActive
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar Test...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                IconButton(
                    onClick = { isFiltering = !isFiltering },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isFiltering) Color(0xFFE0E0E0) else Color.White,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = if (isFiltering) Color(0xFF003AB6) else Color(0xFF4A4A4A)
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isFiltering,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Filtros de Tests", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        Box(Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = filterActiveExpanded,
                                onExpandedChange = { filterActiveExpanded = !filterActiveExpanded }
                            ) {
                                val activeText = when (filterActive) {
                                    true -> "Activa"
                                    false -> "Inactiva"
                                    else -> "Todas"
                                }
                                OutlinedTextField(
                                    value = activeText,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estado", fontSize = 12.sp) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = filterActiveExpanded
                                        )
                                    },
                                    modifier = Modifier.menuAnchor(
                                        MenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ).fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = filterActiveExpanded,
                                    onDismissRequest = { filterActiveExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Todas") },
                                        onClick = {
                                            filterActive = null; filterActiveExpanded = false
                                        })
                                    DropdownMenuItem(
                                        text = { Text("Activa") },
                                        onClick = {
                                            filterActive = true; filterActiveExpanded = false
                                        })
                                    DropdownMenuItem(
                                        text = { Text("Inactiva") },
                                        onClick = {
                                            filterActive = false; filterActiveExpanded = false
                                        })
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                filterActive = null
                                searchQuery = ""
                                onFilter(FilterTestsDto())
                            }) { Text("Limpiar") }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("WelcomeTest global", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "Puedes asociar varios WelcomeTest, pero solo uno puede estar activo.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (welcomeT.isEmpty()) {
                        Text("No hay WelcomeTest asociados.", color = Color.Gray)
                    } else {
                        welcomeT.forEach { test ->
                            val isActiveWelcome = test.id == activeWelcomeTestId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(TestDetailsScreen(test.id)) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            test.name,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2D5E3D),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            test.description,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Badge(
                                        containerColor = if (isActiveWelcome) Color(0xFFB8F4C4) else Color(0xFFE0E0E0),
                                        contentColor = if (isActiveWelcome) Color(0xFF2D5E3D) else Color(0xFF4A4A4A)
                                    ) {
                                        Text(if (isActiveWelcome) "Activo" else "Inactivo")
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (!isActiveWelcome) {
                                            IconButton(
                                                 onClick = { onActivateWelcome(test.id) },
                                                 enabled = !isLoading,
                                                 modifier = Modifier.size(28.dp)
                                             ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Seleccionar activo",
                                                    tint = Color(0xFF2D5E3D)
                                                )
                                             }
                                        }

                                        IconButton(
                                             onClick = { onDeleteWelcome(test.id) },
                                             enabled = !isLoading,
                                             modifier = Modifier.size(28.dp)
                                         ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Desvincular WelcomeTest",
                                                tint = Color(0xFF8B0000)
                                            )
                                         }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { welcomeMode = WelcomeMode.SELECT_EXISTING },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8F4C4)),
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Asociar WelcomeTest", color = Color(0xFF2D5E3D))
                    }

                    if (welcomeMode != WelcomeMode.NONE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { welcomeMode = WelcomeMode.SELECT_EXISTING },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (welcomeMode == WelcomeMode.SELECT_EXISTING) Color(
                                        0xFF003AB6
                                    ) else Color(0xFFE0E0E0)
                                )
                            ) {
                                Text(
                                    "Seleccionar existente",
                                    color = if (welcomeMode == WelcomeMode.SELECT_EXISTING) Color.White else Color.Black
                                )
                            }
                            Button(
                                onClick = { welcomeMode = WelcomeMode.CREATE_NEW },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (welcomeMode == WelcomeMode.CREATE_NEW) Color(
                                        0xFF003AB6
                                    ) else Color(0xFFE0E0E0)
                                )
                            ) {
                                Text(
                                    "Crear nuevo",
                                    color = if (welcomeMode == WelcomeMode.CREATE_NEW) Color.White else Color.Black
                                )
                            }
                        }

                        when (welcomeMode) {
                            WelcomeMode.SELECT_EXISTING -> {
                                ExposedDropdownMenuBox(
                                    expanded = existingWelcomeMenu,
                                    onExpandedChange = {
                                        existingWelcomeMenu = !existingWelcomeMenu
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = selectedExistingWelcomeTest?.name
                                            ?: "Seleccionar test",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = existingWelcomeMenu
                                            )
                                        },
                                        modifier = Modifier.menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        ).fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = existingWelcomeMenu,
                                        onDismissRequest = { existingWelcomeMenu = false }
                                    ) {
                                        existingWelcomeCandidates.forEach { candidate ->
                                            DropdownMenuItem(
                                                text = { Text(candidate.name) },
                                                onClick = {
                                                    selectedExistingWelcomeTest = candidate
                                                    existingWelcomeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                if (existingWelcomeCandidates.isEmpty()) {
                                    Text(
                                        "No hay tests disponibles para asociar.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedExistingWelcomeTest?.let {
                                            onSetWelcome(it.id)
                                            welcomeMode = WelcomeMode.NONE
                                            selectedExistingWelcomeTest = null
                                        }
                                    },
                                    enabled = selectedExistingWelcomeTest != null && !isLoading
                                ) { Text("Asociar como WelcomeTest") }
                            }

                            WelcomeMode.CREATE_NEW -> {
                                OutlinedTextField(
                                    value = newWelcomeName,
                                    onValueChange = { newWelcomeName = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = newWelcomeDescription,
                                    onValueChange = { newWelcomeDescription = it },
                                    label = { Text("Descripción") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )

                                Text(
                                    "Este WelcomeTest puede incluir ejercicios de cualquier unidad.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                OutlinedTextField(
                                    value = searchWelcomeExercise,
                                    onValueChange = { searchWelcomeExercise = it },
                                    label = { Text("Buscar ejercicios") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    itemsIndexed(exercisesForNewWelcome) { _, exercise ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedWelcomeExercises =
                                                        if (selectedWelcomeExercises.contains(exercise.id)) {
                                                            selectedWelcomeExercises - exercise.id
                                                        } else {
                                                            selectedWelcomeExercises + exercise.id
                                                        }
                                                }
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            androidx.compose.material3.Checkbox(
                                                checked = selectedWelcomeExercises.contains(exercise.id),
                                                onCheckedChange = { checked ->
                                                    selectedWelcomeExercises = if (checked) {
                                                        selectedWelcomeExercises + exercise.id
                                                    } else {
                                                        selectedWelcomeExercises - exercise.id
                                                    }
                                                }
                                            )
                                            Column {
                                                Text(exercise.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    exercise.description ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                    }
                                }

                                Button(
                                    onClick = {
                                        val unitId = defaultUnitIdForWelcome
                                        if (unitId == null) {
                                            onError(Error("No hay unidades disponibles para crear el WelcomeTest"))
                                            return@Button
                                        }

                                        onCreateWelcome(
                                            CreateTestDto(
                                                unitId = unitId,
                                                name = newWelcomeName,
                                                description = newWelcomeDescription,
                                                isActive = true
                                            ),
                                            selectedWelcomeExercises.toList()
                                        )

                                        newWelcomeName = ""
                                        newWelcomeDescription = ""
                                        searchWelcomeExercise = ""
                                        selectedWelcomeExercises = emptySet()
                                        welcomeMode = WelcomeMode.NONE
                                    },
                                    enabled = !isLoading && newWelcomeName.isNotBlank() && newWelcomeDescription.isNotBlank() && defaultUnitIdForWelcome != null,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Crear y asociar WelcomeTest") }
                            }

                            WelcomeMode.NONE -> Unit
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isAddingTest = !isAddingTest },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAddingTest) Color(
                            0xFFE0E0E0
                        ) else Color(0xFFB8F4C4)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "+ Agregar nuevo test",
                        color = Color(0xFF2D5E3D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isAddingTest,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Nuevo Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = nameTest,
                            onValueChange = { nameTest = it },
                            label = { Text("Nombre del test") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = nameTest.isBlank()
                        )

                        OutlinedTextField(
                            value = descriptionTest,
                            onValueChange = { descriptionTest = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = descriptionTest.isBlank()
                        )

                        ExposedDropdownMenuBox(
                            expanded = unitMenu,
                            onExpandedChange = { unitMenu = !unitMenu }
                        ) {
                            OutlinedTextField(
                                value = selectedUnit?.name ?: "Seleccionar Unidad",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenu) },
                                modifier = Modifier.menuAnchor(
                                    MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                ).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = unitMenu,
                                onDismissRequest = { unitMenu = false }
                            ) {
                                allUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.name) },
                                        onClick = {
                                            selectedUnit = unit
                                            unitMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { isAddingTest = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFFD4D4
                                    )
                                )
                            ) { Text("Cancelar", color = Color(0xFF8B0000)) }

                            Button(
                                onClick = {
                                    if (nameTest.isBlank() || descriptionTest.isBlank()) {
                                        onError(Error("El nombre y la descripción son obligatorios"))
                                        return@Button
                                    }

                                    onCreateTest(
                                        CreateTestDto(
                                            unitId = selectedUnit?.id ?: -1,
                                            name = nameTest,
                                            description = descriptionTest,
                                            isActive = false
                                        )
                                    )

                                    nameTest = ""
                                    descriptionTest = ""
                                    selectedUnit = null
                                    isAddingTest = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFB8F4C4
                                    )
                                )
                            ) { Text("Guardar Test", color = Color(0xFF2D5E3D)) }
                        }
                    }
                }
            }
        }

        if (allTests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron tests", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            itemsIndexed(allTests) { _, test ->
                TestCard(test = test, onClick = { onNavigate(TestDetailsScreen(test.id)) })
            }
        }
    }
}

private enum class WelcomeMode {
    NONE,
    SELECT_EXISTING,
    CREATE_NEW
}

@Composable
fun TestCard(
    test: TestDto,
    onClick: () -> Unit = {}
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FactCheck,
                    contentDescription = "Test Icon",
                    tint = Color(0xFF2D5E3D),
                    modifier = Modifier.size(40.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = test.name,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D),
                            fontSize = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            fontFamily = encodeSansFamily,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val (badgeColor, badgeTextColor, badgeLabel) = if (test.isActive) {
                            Triple(Color(0xFFB8F4C4), Color(0xFF2D5E3D), "Publicado")
                        } else {
                            Triple(Color(0xFFFFD4D4), Color(0xFF8B0000), "Borrador")
                        }

                        Badge(containerColor = badgeColor, contentColor = badgeTextColor) {
                            Text(badgeLabel, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = test.description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = jetbrainsMonoFamily
                    )
                }
            }
        }
    }
}
