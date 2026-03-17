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
import androidx.compose.material.icons.automirrored.filled.Send
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
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.CreateTestDto
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.DifficultyLevel
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.TestDto
import org.example.project.dtos.UnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.TestViewModel
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

class TestScreen: Screen{
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val testVm = rememberScreenModel { TestViewModel(RepositoryProvider.testRepo) }
        val testUi by testVm.state.collectAsState()

        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val unitUi by unitVm.state.collectAsState()

        var selectedIndex by remember { mutableStateOf(4) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(testUi.error) {
            testUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        LaunchedEffect(Unit){
            testVm.getAllTests()
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
                    tests = testUi.selectedTests,
                    allTests = testUi.allTests, // Pasamos la lista de tests como datos reales
                    allUnits = unitUi.units,
                    onCreateTest = { testVm.createTest(it) },
                    onNavigate = { navigator.push(it) },
                    onFilter = { /*TODO*/ },
                    onError = { testVm.updateMessage(it.message) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSection(
    tests: List<TestDto>,
    allTests: List<TestDto>, // Lista origen datos reales
    allUnits : List<UnitDto> = emptyList(), // Lista de unidades para el dropdown
    onCreateTest: (CreateTestDto) -> Unit,
    onFilter: (FilterUnitsDto) -> Unit, // Callback para filtros
    onNavigate : (Screen) -> Unit = {},
    onError: (Error) -> Unit
) {
    // Estados para filtrado
    var searchQuery by remember { mutableStateOf("") }
    var isFiltering by remember { mutableStateOf(false) }
    var filterDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }
    var filterDifficultyExpanded by remember { mutableStateOf(false) }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    var filterActiveExpanded by remember { mutableStateOf(false) }

    var isAddingTest by remember { mutableStateOf(false) }
    var nameTest by remember { mutableStateOf("") }
    var descriptionTest by remember { mutableStateOf("") }
    var unitId by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<UnitDto?>(null) }

    var unitMenu by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp)
    ) {
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
                trailingIcon = {
                    IconButton(onClick = { /*Filtros de Tests */}) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar búsqueda",
                            tint = Color(0xFF4A4A4A).copy(alpha = 0.5f)
                        )
                    }
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
                    Text("Filtros de Tests", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Estado (Activo/Inactivo)
                        Box(Modifier.weight(1f)) {
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
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterActiveExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                )
                                ExposedDropdownMenu(
                                    expanded = filterActiveExpanded,
                                    onDismissRequest = { filterActiveExpanded = false }
                                ) {
                                    DropdownMenuItem(text = { Text("Todas") }, onClick = { filterActive = null; filterActiveExpanded = false })
                                    DropdownMenuItem(text = { Text("Activa") }, onClick = { filterActive = true; filterActiveExpanded = false })
                                    DropdownMenuItem(text = { Text("Inactiva") }, onClick = { filterActive = false; filterActiveExpanded = false })
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            filterDifficulty = null
                            filterActive = null
                            searchQuery = ""
                            onFilter(FilterUnitsDto())
                        }) { Text("Limpiar") }
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                onFilter(FilterUnitsDto(name = searchQuery,isActive = filterActive))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003AB6)),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Aplicar") }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAddingTest = !isAddingTest },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isAddingTest) Color(0xFFE0E0E0) else Color(0xFFB8F4C4)
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

        // Formulario desplegable para agregar test
        AnimatedVisibility(
            visible = isAddingTest,
            enter = expandVertically(),
            exit = shrinkVertically()
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
                    Text("Nuevo Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = nameTest,
                        onValueChange = { nameTest = it },
                        label = { Text("Nombre de del test") },
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
                            value = selectedUnit?.name?: "Seleccionar Unidad",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = unitMenu
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
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
                            // opción para limpiar rol
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    selectedUnit = null
                                    unitMenu = false
                                }
                            )
                        }
                    }
                    Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {isAddingTest = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD4D4)
                            )
                        ) {
                            Text("Cancelar", color = Color(0xFF8B0000))
                        }

                        Button(
                            onClick = {
                                if (nameTest.isBlank() || descriptionTest.isBlank()) {
                                    onError(Error("El nombre y la descripción son obligatorios"))
                                    return@Button
                                }

                                // Ya se validó arriba que no sean nulos
                                onCreateTest(
                                    CreateTestDto(
                                        unitId = selectedUnit?.id ?: -1, // Si no se selecciona unidad, se asigna 0 o se puede manejar de otra forma
                                        name = nameTest,
                                        description = descriptionTest,
                                        isActive = false, // Por defecto activo
                                    )
                                )

                                // Limpiar campos y cerrar formulario
                                nameTest = ""
                                descriptionTest = ""
                                isAddingTest = false

                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB8F4C4)
                            )
                        ) {
                            Text("Guardar Test", color = Color(0xFF2D5E3D))
                        }

                    }
                }
            }
        }
        if(tests.isEmpty()){
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron tests", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(tests) { index, test ->
                    TestCard(
                        test = test,
                        onClick = {onNavigate(TestDetailsScreen(test.id))}
                    )
                }
            }
        }
    }
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
            // Contenido principal: emoji a la izquierda, título + badge a la derecha del emoji, descripción debajo
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FactCheck, // Aquí puedes usar un icono específico para tests
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