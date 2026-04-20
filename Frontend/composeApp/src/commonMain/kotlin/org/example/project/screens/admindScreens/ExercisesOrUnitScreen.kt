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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
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
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.Navigator
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.CreateExerciseDto
import org.example.project.dtos.ExerciseDto
import org.example.project.dtos.FilterExercisesDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.dtos.UpdateUnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

class ExercisesOrUnitScreen(private val unitId: Int? = null) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val exerciseVm = rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo, unitId) }
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo, unitId) }
        val unitUi by unitVm.state.collectAsState()
        val exerciseUi by exerciseVm.state.collectAsState()

        val testVm = rememberScreenModel {
            TestViewModel(
                RepositoryProvider.testRepo,
                RepositoryProvider.welcomeTestRepo
            )
        }
        val testUi by testVm.state.collectAsState()

        var selectedIndex by remember { mutableStateOf(2) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(exerciseUi.error) {
            exerciseUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        LaunchedEffect(unitId) {
            if (unitId != null) {
                unitVm.getUnitById(unitId)
                exerciseVm.getExercisesByUnitId(unitId)
                selectedIndex = 2
                testVm.getTestsByUnitId(unitId)
            } else {
                unitVm.actualNull()
                unitVm.getAllUnits()
                exerciseVm.getAllExercises()
                selectedIndex = 3
            }
            // Obtener mapeo de ejercicios en tests
            testVm.getAllExercisesInTests()
        }


        var searchQuery by remember { mutableStateOf("") }

        AppLayout(
            actualScreen = "Administrar Ejercicios",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->
            // ...existing code...
            Column(
                modifier = Modifier
                    .fillMaxSize(),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExercisesSection(
                    modifier = Modifier.weight(1f),
                    navigator = navigator,
                    actualUnit = unitUi.actualUnit,
                    showUnitHeader = unitId != null,
                    exercises = exerciseUi.exercises,
                    testUnit = testUi.currentTest,
                    allTestExercises = testUi.allTestExercises,
                    allTests = testUi.allTests,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onAdd = { dto ->
                        exerciseVm.createExercise(dto)
                    },
                    onUnitEdit = { id, dto ->
                        unitVm.updateUnit(id, dto)
                    },
                    unitsList = unitUi.units,
                    onReorder = { updates ->
                        exerciseVm.reorderExercises(updates)
                    },
                    onError = { error -> exerciseVm.updateMessage(error.message) },
                    onFilter = { filterDto -> exerciseVm.searchExercises(filterDto) }
                )
            }
        }

    }
}


// Section que contiene búsqueda, botones y lista de ejercicios
@OptIn(ExperimentalMaterial3Api::class) // Necesario para dropdowns y cards experimentales si aplica
@Composable
fun ExercisesSection(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    actualUnit: UnitDto? = null,
    unitsList: List<UnitDto> = emptyList(),
    testUnit: TestDto? = null,
    allTestExercises: Map<Int, List<ExerciseDto>> = emptyMap(),
    allTests: List<TestDto> = emptyList(),
    showUnitHeader: Boolean = false,
    exercises: List<ExerciseDto>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAdd: (CreateExerciseDto) -> Unit, // Cambiado para recibir DTO
    onUnitEdit: (Int, UpdateUnitDto) -> Unit, // Nuevo callback para editar ejercicio
    onReorder: (List<Pair<Int, Int>>) -> Unit, // Nuevo callback para reordenar
    onFilter: (FilterExercisesDto) -> Unit, // Callback para fitros backend
    onError: (Error) -> Unit,
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))
    
    // Crear mapeo de exerciseId -> testName
    val exerciseToTestNameMap = remember(allTestExercises, allTests) {
        val map = mutableMapOf<Int, String>()
        allTestExercises.forEach { (testId, exercisesInTest) ->
            val testName = allTests.find { it.id == testId }?.name ?: ""
            exercisesInTest.forEach { exercise ->
                map[exercise.id] = testName
            }
        }
        map
    }

    // Estados para agregar ejercicio
    var isAddingExercise by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var newExerciseDescription by remember { mutableStateOf("") }
    var unitMenu by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf<UnitDto?>(null) }


    var isEditingUnit by remember { mutableStateOf(false) }
    var editStatus by remember { mutableStateOf(false) }
    var nameUnit by remember { mutableStateOf(actualUnit?.name) }
    var descriptionUnit by remember { mutableStateOf(actualUnit?.description) }
    var activeUnit by remember { mutableStateOf(actualUnit?.isActive ?: false) }

    // Estados para filtrado
    var isFiltering by remember { mutableStateOf(false) }
    var filterUnit by remember { mutableStateOf<UnitDto?>(null) }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    var filterActiveExpanded by remember { mutableStateOf(false) }
    var filterUnitExpanded by remember { mutableStateOf(false) }
    var skipFirstFilterEffect by remember { mutableStateOf(true) }

    // UI estados para el botón
    var textAdd by remember { mutableStateOf("") }
    var butonAddColor by remember { mutableStateOf(Color(0xFFB8F4C4)) }

    // Estados para reordenar
    var isReordering by remember { mutableStateOf(false) }
    var reorderableList by remember { mutableStateOf(exercises) } // Inicialmente la lista original

    // Sincronizar lista local cuando cambian los datos (si no estamos reordenando)
    LaunchedEffect(exercises) {
        if (!isReordering) {
            reorderableList = exercises
        }
    }
    LaunchedEffect(actualUnit) {
        if (actualUnit != null) {
            nameUnit = actualUnit.name
            descriptionUnit = actualUnit.description
            activeUnit = actualUnit.isActive
            if (showUnitHeader) {
                // Si la pantalla ya trae unidad, el filtro por unidad queda fijo.
                filterUnit = actualUnit
            }
        }
    }

    LaunchedEffect(searchQuery, filterActive, filterUnit?.id, isReordering, showUnitHeader, actualUnit?.id) {
        if (skipFirstFilterEffect) {
            skipFirstFilterEffect = false
            return@LaunchedEffect
        }
        if (showUnitHeader && actualUnit?.id == null) return@LaunchedEffect
        if (!isReordering) {
            onFilter(
                FilterExercisesDto(
                    name = searchQuery,
                    isActive = filterActive,
                    unitId = if (showUnitHeader) actualUnit?.id else filterUnit?.id
                )
            )
        }
    }

    if (isAddingExercise) {
        textAdd = "Cancelar creación"
        butonAddColor = Color(0xFFFFD4D4)
    } else {
        textAdd = "+ Agregar nuevo ejercicio"
        butonAddColor = Color(0xFFB8F4C4)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (showUnitHeader && actualUnit != null) {

                    Row(
                        modifier = Modifier.fillMaxWidth(), // Align items to the top
                    ) {
                        // 1. Emoji Column
                        Text(
                            text = "📚",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp, top = 4.dp)
                        )

                        // 2. Middle Column: Name, Badge, Description
                        Column(
                            modifier = Modifier.weight(1f) // Takes available space
                        ) {
                            // Title Row: Name + Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(
                                            1f,
                                            fill = false
                                        ) // Allow text to wrap/shrink, don't force full width if short
                                        .padding(end = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = nameUnit ?: "Nombre de la unidad",
                                        onValueChange = { nameUnit = it },
                                        readOnly = !isEditingUnit,
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = encodeSansFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF131313)
                                        ),
                                        maxLines = 2, // Allow title to wrap a bit
                                        modifier = if (isEditingUnit) {
                                            Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color.Transparent,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(6.dp)
                                                .border(
                                                    1.dp,
                                                    Color(0xFFE0E0E0),
                                                    shape = RoundedCornerShape(4.dp),
                                                )
                                        } else {
                                            Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color.Transparent,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(6.dp)
                                        }
                                    )
                                }

                                // Badge
                                if (!isEditingUnit) {
                                    Badge(
                                        containerColor = if (activeUnit) Color(0xFFB8F4C4) else Color(
                                            0xFFFFD4D4
                                        ),
                                        contentColor = if (activeUnit) Color(0xFF2D5E3D) else Color(
                                            0xFF8B0000
                                        ),
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Text(
                                            if (activeUnit) "Activo" else "Inactivo",
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    ExposedDropdownMenuBox(
                                        expanded = editStatus,
                                        onExpandedChange = { editStatus = !editStatus }
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .menuAnchor(
                                                    MenuAnchorType.PrimaryNotEditable,
                                                    enabled = true
                                                )
                                                .height(28.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (activeUnit) Color(0xFFB8F4C4) else Color(
                                                0xFFFFD4D4
                                            ),
                                            border = null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = if (activeUnit) "Activo" else "Inactivo",
                                                    fontSize = 12.sp,
                                                    color = if (activeUnit) Color(0xFF2D5E3D) else Color(
                                                        0xFF8B0000
                                                    ),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = if (editStatus) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (activeUnit) Color(0xFF2D5E3D) else Color(
                                                        0xFF8B0000
                                                    )
                                                )
                                            }
                                        }
                                        // El menú desplegable
                                        ExposedDropdownMenu(
                                            expanded = editStatus,
                                            onDismissRequest = { editStatus = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Activo", color = Color(0xFF2D5E3D)) },
                                                onClick = {
                                                    activeUnit = true
                                                    editStatus = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Inactivo", color = Color(0xFF8B0000)) },
                                                onClick = {
                                                    activeUnit = false
                                                    editStatus = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Description
                            BasicTextField(
                                value = descriptionUnit ?: "Descripcion de la unidad",
                                onValueChange = { descriptionUnit = it },
                                readOnly = !isEditingUnit,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = jetbrainsMonoFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4A4A4A),
                                    lineHeight = 20.sp
                                ),
                                modifier = if (isEditingUnit) {
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp) // Max height approx 5-6 lines
                                        .verticalScroll(rememberScrollState())
                                        .background(
                                            Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(6.dp)
                                        .border(
                                            1.dp,
                                            Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .verticalScroll(rememberScrollState())
                                        .background(
                                            Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(6.dp)
                                }
                            )
                        }

                        // 3. Right Column: Action Button
                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            if (isEditingUnit) {
                                IconButton(
                                    onClick = {
                                        isEditingUnit = !isEditingUnit
                                        actualUnit.id?.let {
                                            onUnitEdit(
                                                it,
                                                UpdateUnitDto(
                                                    name = nameUnit ?: "",
                                                    description = descriptionUnit ?: "",
                                                    isActive = activeUnit
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).background(
                                        shape = RoundedCornerShape(50.dp),
                                        color = Color(0xFFB8F4C4)
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Guardar unidad",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF2D5E3D) // Darker green for visibility
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { isEditingUnit = !isEditingUnit },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Unidad",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Column {
                        Text("Ejercicios asociados")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            item {
                // Barra de búsqueda y filtro (Visible si no reordenamos)
                if (!isReordering) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Buscar ejercicios...",
                                        fontSize = 14.sp
                                    )
                                },
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
                                    tint = if (isFiltering) Color(0xFF003AB6) else Color(
                                        0xFF4A4A4A
                                    )
                                )
                            }
                        }

                        // Formulario de Filtros Expandible
                        AnimatedVisibility(
                            visible = isFiltering,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Filtros de Ejercicios",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Unidad
                                        Box(Modifier.weight(1f)) {
                                            if (showUnitHeader) {
                                                OutlinedTextField(
                                                    value = actualUnit?.name ?: "Unidad fija",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    enabled = false,
                                                    label = { Text("Unidad (bloqueada)", fontSize = 12.sp) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledContainerColor = Color(0xFFF4F4F4),
                                                        disabledBorderColor = Color(0xFFDADADA),
                                                        disabledTextColor = Color(0xFF616161),
                                                        disabledLabelColor = Color(0xFF757575)
                                                    )
                                                )
                                            } else {
                                                ExposedDropdownMenuBox(
                                                    expanded = filterUnitExpanded,
                                                    onExpandedChange = {
                                                        filterUnitExpanded = !filterUnitExpanded
                                                    }
                                                ) {
                                                    OutlinedTextField(
                                                        value = filterUnit?.name ?: "Todas",
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text("Unidad", fontSize = 12.sp) },
                                                        trailingIcon = {
                                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                                expanded = filterUnitExpanded
                                                            )
                                                        },
                                                        modifier = Modifier
                                                            .menuAnchor(
                                                                MenuAnchorType.PrimaryNotEditable,
                                                                enabled = true
                                                            )
                                                            .fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedContainerColor = Color.White,
                                                            unfocusedContainerColor = Color.White
                                                        )
                                                    )
                                                    ExposedDropdownMenu(
                                                        expanded = filterUnitExpanded,
                                                        onDismissRequest = { filterUnitExpanded = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Todas") },
                                                            onClick = {
                                                                filterUnit = null
                                                                filterUnitExpanded = false
                                                            }
                                                        )
                                                        unitsList.forEach { unitOption ->
                                                            DropdownMenuItem(
                                                                text = { Text(unitOption.name) },
                                                                onClick = {
                                                                    filterUnit = unitOption
                                                                    filterUnitExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Estado
                                        Box(Modifier.weight(1f)) {
                                            ExposedDropdownMenuBox(
                                                expanded = filterActiveExpanded,
                                                onExpandedChange = {
                                                    filterActiveExpanded = !filterActiveExpanded
                                                }
                                            ) {
                                                val activeText = when (filterActive) {
                                                    true -> "Activo"
                                                    false -> "Inactivo"
                                                    else -> "Todos"
                                                }
                                                OutlinedTextField(
                                                    value = activeText,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = {
                                                        Text(
                                                            "Estado",
                                                            fontSize = 12.sp
                                                        )
                                                    },
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
                                                    onDismissRequest = {
                                                        filterActiveExpanded = false
                                                    }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Todos") },
                                                        onClick = {
                                                            filterActive = null; filterActiveExpanded =
                                                            false
                                                        })
                                                    DropdownMenuItem(
                                                        text = { Text("Activo") },
                                                        onClick = {
                                                            filterActive = true; filterActiveExpanded =
                                                            false
                                                        })
                                                    DropdownMenuItem(
                                                        text = { Text("Inactivo") },
                                                        onClick = {
                                                            filterActive = false; filterActiveExpanded =
                                                            false
                                                        })
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            if (!showUnitHeader) {
                                                filterUnit = null
                                            }
                                            filterActive = null
                                            onSearchQueryChange("")
                                            onFilter(
                                                FilterExercisesDto(
                                                    name = null,
                                                    isActive = null,
                                                    unitId = if (showUnitHeader) actualUnit?.id else null
                                                )
                                            )
                                        }) { Text("Limpiar") }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    Text(
                        "Modo Ordenamiento: Usa las flechas para mover",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003AB6),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            item {
                // Botón de agregar nuevo ejercicio y ordenar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isReordering) {
                        Button(
                            onClick = {
                                if (unitsList.isEmpty()) {
                                    onError(Error("Debe existir al menos una unidad para crear ejercicios"))
                                } else {
                                    isAddingExercise = !isAddingExercise
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = butonAddColor
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {

                            Text(
                                textAdd,
                                color = Color(0xFF2D5E3D),
                                fontWeight = FontWeight.Medium
                            )
                        }


                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (!showUnitHeader) {
                        OutlinedButton(
                            onClick = {

                                if (isReordering) {
                                    isAddingExercise = false
                                    val updates =
                                        reorderableList.mapIndexed { index, exItem ->
                                            // Asumimos que id es válido, y index+1 es el nuevo orden
                                            Pair(exItem.id, index + 1)
                                        }
                                    onReorder(updates)
                                    println("Reordenando unidades con el siguiente orden: $updates")
                                    isReordering = false
                                } else {
                                    isReordering = true
                                    // Al iniciar reordenamiento, aseguramos usar toda la lista
                                    reorderableList =
                                        exercises.sortedBy { it.orderExercise }
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isReordering) Color(0xFF003AB6) else Color.Transparent,
                                contentColor = if (isReordering) Color.White else Color(
                                    0xFF4A4A4A
                                )
                            )
                        ) {
                            if (isReordering) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar Orden")
                            } else {
                                Text("Orden")
                            }
                        }
                    }
                }
            }

            item {
                // Formulario desplegable para agregar ejercicio
                AnimatedVisibility(
                    visible = isAddingExercise,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                    // modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Nuevo Ejercicio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            OutlinedTextField(
                                value = newExerciseName,
                                onValueChange = { newExerciseName = it },
                                label = { Text("Nombre del ejercicio*") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = newExerciseName.isBlank()
                            )

                            OutlinedTextField(
                                value = newExerciseDescription,
                                onValueChange = { newExerciseDescription = it },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = newExerciseDescription.isBlank()
                            )
                            if (actualUnit != null && selectedUnit == null) {
                                selectedUnit = actualUnit
                            }
                            ExposedDropdownMenuBox(
                                expanded = unitMenu,
                                onExpandedChange = { unitMenu = !unitMenu }
                            ) {

                                OutlinedTextField(
                                    value = selectedUnit?.name
                                        ?: "Selecciona una unidad",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unidad asociada") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = unitMenu
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        )
                                        .fillMaxWidth(),
                                    isError = selectedUnit == null
                                )

                                ExposedDropdownMenu(
                                    expanded = unitMenu,
                                    onDismissRequest = { unitMenu = false }
                                ) {
                                    unitsList.forEach { unitOp ->
                                        DropdownMenuItem(
                                            text = { Text(unitOp.name) },
                                            onClick = {
                                                selectedUnit = unitOp
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
                                    onClick = { isAddingExercise = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFD4D4)
                                    )
                                ) {
                                    Text("Cancelar", color = Color(0xFF8B0000))
                                }
                                Button(
                                    onClick = {
                                        if (newExerciseName.isBlank() || newExerciseDescription.isBlank()) {
                                            onError(Error("Campos: Nombre, descripción y unidad son obligatorios"))
                                            return@Button
                                        }
                                        if (selectedUnit == null) {
                                            onError(Error("No hay unidad seleccionada"))
                                            return@Button
                                        }

                                        selectedUnit?.id?.let { id ->
                                            onAdd(
                                                CreateExerciseDto(
                                                    name = newExerciseName,
                                                    description = newExerciseDescription,
                                                    unitId = id,
                                                )
                                            )
                                        } ?: onError(Error("Unidad seleccionada no es válida"))

                                        // Reset fields
                                        newExerciseName = ""
                                        newExerciseDescription = ""
                                        selectedUnit = null
                                        isAddingExercise = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFFB8F4C4
                                        )
                                    )
                                ) {
                                    Text(
                                        "Guardar Ejercicio",
                                        color = Color(0xFF2D5E3D)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (exercises.isEmpty() && !isReordering) {
                item { Text("No hay ejercicios", color = Color.Gray) }
            } else {
                
                if (isReordering) {
                    itemsIndexed(reorderableList) { index, exItem ->
                        val isFirst = index == 0
                        val isLast = index == reorderableList.lastIndex

                        ReorderableUnitCard(
                            name = exItem.name,
                            index = index + 1,
                            isFirst = isFirst,
                            isLast = isLast,
                            onMoveUp = {
                                if (!isFirst) {
                                    val mutable = reorderableList.toMutableList()
                                    val current = mutable[index]
                                    val previous = mutable[index - 1]
                                    mutable[index] = previous
                                    mutable[index - 1] = current
                                    reorderableList = mutable
                                }
                            },
                            onMoveDown = {
                                if (!isLast) {
                                    val mutable = reorderableList.toMutableList()
                                    val current = mutable[index]
                                    val next = mutable[index + 1]
                                    mutable[index] = next
                                    mutable[index + 1] = current
                                    reorderableList = mutable
                                }
                            }
                        )
                    }
                } else {
                    itemsIndexed(exercises) { _, ex ->
                        ExerciseCard(
                            exercises = ex,
                            testName = exerciseToTestNameMap[ex.id],
                            onExerciseClick = {
                                navigator.push(
                                    ExercisesDetailsScreen(
                                        ex.id,
                                        ex.unitId
                                    )
                                )
                            },
                        )
                    }
                }

            }
            
            if(showUnitHeader){
                item {
                    Column {
                        Text("Test asociado")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(4.dp))

                    if (testUnit == null) {
                        Text("No hay test asociados a esta unidad", color = Color.Gray)
                    } else {
                        TestCard(
                            test = testUnit,
                            onClick = {
                                navigator.push(
                                    TestDetailsScreen(
                                        testUnit.id
                                    )
                                )
                            }
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercises: ExerciseDto,
    testName: String? = null,
    onExerciseClick: () -> Unit = {}
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExerciseClick() }
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
            // Contenido principal: emoji a la izquierda, título + badges a la derecha
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📝",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = exercises.name,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D),
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = encodeSansFamily,
                        )

                        val (badgeColor, badgeTextColor, badgeLabel) = if (exercises.isActive) {
                            Triple(Color(0xFFB8F4C4), Color(0xFF2D5E3D), "Publicado")
                        } else {
                            Triple(Color(0xFFFFD4D4), Color(0xFF8B0000), "Borrador")
                        }

                        Badge(containerColor = badgeColor, contentColor = badgeTextColor) {
                            Text(badgeLabel, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badge del test si existe
                    if (testName != null) {
                        Badge(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF0D47A1)
                        ) {
                            Text("Test: $testName", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    exercises.description?.let {
                        Text(
                            text = it,
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
}


@Composable
fun ExerciseCard2(
    exercise: ExerciseDto,
    onClick: () -> Unit = {},

    ) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = encodeSansFamily
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val (badgeColor, badgeTextColor, badgeLabel) = if (exercise.isActive) {
                        Triple(Color(0xFFB8F4C4), Color(0xFF2D5E3D), "Publicado")
                    } else {
                        Triple(Color(0xFFFFD4D4), Color(0xFF8B0000), "Borrador")
                    }

                    Badge(containerColor = badgeColor, contentColor = badgeTextColor) {
                        Text(badgeLabel, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                exercise.description?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = jetbrainsMonoFamily
                    )
                }
            }
        }
    }
}