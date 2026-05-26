package org.example.project.screens.editorScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.Navigator
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.EditorLayout
import org.example.project.components.EditorContentCard
import org.example.project.dtos.CreateUnitDto
import org.example.project.dtos.DifficultyLevel
import org.example.project.dtos.FilterUnitsDto
import org.example.project.dtos.UnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.screens.admindScreens.LessonUnit
import org.example.project.screens.admindScreens.UnitStatus
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

// Definiciones de fuentes (ver comentarios en DashboardAdmin.kt)


class UnitsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo) }
        val unitUi by unitVm.state.collectAsState()
        var selectedIndex by remember { mutableStateOf(2) }
        val snackbarHostState = remember { SnackbarHostState() }


        LaunchedEffect(unitUi.error) {
            unitUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        LaunchedEffect(Unit){
            unitVm.getAllUnits()
        }


        EditorLayout(
            actualScreen = "Administrar Unidades",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                // Usamos la UnitsSection tal como la pediste
                UnitsSection(
                    navigator = navigator,
                    allUnitsDto = unitUi.units, // Pasamos la lista original DTO para poder actualizar
                    onCreateUnit = { dto -> unitVm.createUnit(dto) },
                    onReorderUnits = { updatedList -> unitVm.updateUnitsOrder(updatedList) }, // Callback
                    onError = { error -> unitVm.updateMessage(error.message) },
                    onFilter = { filterDto -> unitVm.searchUnits(filterDto) } // Callback para búsqueda backend
                )
            }
        }
    }
}

// Implementación de UnitsSection (según tu especificación)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsSection(
    navigator: Navigator,
    allUnitsDto: List<UnitDto>, // Lista origen datos reales
    onCreateUnit: (CreateUnitDto) -> Unit,
    onReorderUnits: (List<Pair<Int, Int>>) -> Unit,
    onFilter: (FilterUnitsDto) -> Unit, // Callback para filtros
    onError: (Error) -> Unit
) {
    // Estados existentes
    var isAddingUnit by remember { mutableStateOf(false) }
    var newUnitName by remember { mutableStateOf("") }
    var newUnitDescription by remember { mutableStateOf("") }
    var newUnitOrder by remember { mutableStateOf("") }
    var textAdd by remember { mutableStateOf("") }
    var butonAddColor by remember { mutableStateOf(Color(0xFFB8F4C4)) }
    var difficultyMenu by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.A1) }

    // Estados para filtrado
    var searchQuery by remember { mutableStateOf("") }
    var isFiltering by remember { mutableStateOf(false) }
    var filterDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }
    var filterDifficultyExpanded by remember { mutableStateOf(false) }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    var filterActiveExpanded by remember { mutableStateOf(false) }

    // Estado para modo ordenar
    var isReordering by remember { mutableStateOf(false) }
    // Copia local mutable para la UI de reordenamiento
    var reorderableList by remember { mutableStateOf(allUnitsDto.sortedBy { it.orderUnit }) }

    // Sincronizar lista local cuando cambian los datos del servidor (si no estamos reordenando)
    LaunchedEffect(allUnitsDto) {
        if (!isReordering) {
            reorderableList = allUnitsDto.sortedBy { it.orderUnit }
        }
    }

    LaunchedEffect(searchQuery, filterDifficulty, filterActive, isReordering) {
        if (!isReordering) {
            onFilter(
                FilterUnitsDto(
                    name = searchQuery,
                    difficulty = filterDifficulty,
                    isActive = filterActive
                )
            )
        }
    }

    if (isAddingUnit) {
        textAdd = "Cancelar creación"
        butonAddColor = Color(0xFFFFD4D4)
    } else {
        textAdd = "+ Agregar nueva unidad"
        butonAddColor = Color(0xFFB8F4C4)
    }

    // Cambiamos Column por LazyColumn para mejor manejo de listas y reordenamiento visual
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp)
    ) {
        // Barra de búsqueda y filtro (Solo visible si NO estamos reordenando para simplificar UI)
        if (!isReordering) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar unidades...", fontSize = 14.sp) },
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
                        Text("Filtros de Unidades", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Dificultad
                            Box(Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = filterDifficultyExpanded,
                                    onExpandedChange = { filterDifficultyExpanded = !filterDifficultyExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = filterDifficulty?.name ?: "Todas",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Dificultad", fontSize = 12.sp) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterDifficultyExpanded) },
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = filterDifficultyExpanded,
                                        onDismissRequest = { filterDifficultyExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Todas") },
                                            onClick = { filterDifficulty = null; filterDifficultyExpanded = false }
                                        )
                                        DifficultyLevel.entries.forEach { diff ->
                                            DropdownMenuItem(
                                                text = { Text(diff.name) },
                                                onClick = { filterDifficulty = diff; filterDifficultyExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }

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

        // Botón de agregar nueva unidad y Ordenar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ocultar botón agregar si estamos ordenando
            if (!isReordering) {
                Button(
                    onClick = { isAddingUnit = !isAddingUnit },
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

            // Botón Ordenar
            /*OutlinedButton(
                onClick = {
                    if (isReordering) {

                        val updates = reorderableList.mapIndexedNotNull { index, unitDto ->
                            unitDto.id?.let { id ->
                                Pair(id, index + 1)
                            }
                        }
                        println("Reordenando unidades con el siguiente orden: $updates")

                        // Enviamos la lista al backend
                        onReorderUnits(updates)

                        isReordering = false
                    } else {
                        // Activar modo
                        isReordering = true
                        // Aseguramos que empezamos con la lista ordenada actual
                        reorderableList = allUnitsDto.sortedBy { it.orderUnit }
                    }
                },
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isReordering) Color(0xFF003AB6) else Color.Transparent,
                    contentColor = if (isReordering) Color.White else Color(0xFF4A4A4A)
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
            }*/
        }

        // Formulario desplegable para agregar unidad
        AnimatedVisibility(
            visible = isAddingUnit,
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
                    Text("Nueva Unidad", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = newUnitName,
                        onValueChange = { newUnitName = it },
                        label = { Text("Nombre de la unidad*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = newUnitName.isBlank()
                    )


                    OutlinedTextField(
                        value = newUnitDescription,
                        onValueChange = { newUnitDescription = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = newUnitDescription.isBlank()
                    )

                    ExposedDropdownMenuBox(
                        expanded = difficultyMenu,
                        onExpandedChange = { difficultyMenu = !difficultyMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty.name ,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dificultad") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = difficultyMenu
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = difficultyMenu,
                            onDismissRequest = { difficultyMenu = false }
                        ) {
                            DifficultyLevel.entries.forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text(difficulty.name) },
                                    onClick = {
                                        selectedDifficulty = difficulty
                                        difficultyMenu = false
                                    }
                                )
                            }
                            // opción para limpiar rol
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    selectedDifficulty = DifficultyLevel.A1
                                    difficultyMenu = false
                                }
                            )
                        }
                    }
                    Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {isAddingUnit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD4D4)
                            )
                        ) {
                            Text("Cancelar", color = Color(0xFF8B0000))
                        }

                        Button(
                            onClick = {
                                if (newUnitName.isBlank() || newUnitDescription.isBlank()) {
                                    onError(Error("El nombre y la descripción son obligatorios"))
                                    return@Button
                                }

                                // Ya se validó arriba que no sean nulos
                                onCreateUnit(
                                    CreateUnitDto(
                                        name = newUnitName,
                                        description = newUnitDescription,
                                        orderUnit = newUnitOrder.toIntOrNull(),
                                        isActive = false,
                                        difficulty = selectedDifficulty,
                                        createdAt = null
                                    )
                                )

                                // Limpiar campos y cerrar formulario
                                newUnitName = ""
                                newUnitDescription = ""
                                newUnitOrder = ""
                                selectedDifficulty = DifficultyLevel.A1
                                isAddingUnit = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB8F4C4)
                            )
                        ) {
                            Text("Guardar Unidad", color = Color(0xFF2D5E3D))
                        }
                    }
                }
            }
        }

        // LISTA DE UNIDADES
        // Usamos LazyColumn para permitir desplazamiento eficiente y lógica de swap
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f) // Ocupa el espacio restante
        ) {
            if (isReordering) {
                isAddingUnit = false
                // MODO REORDENAR: Usamos la lista local mutable (reorderableList)
                itemsIndexed(reorderableList) { index, unitDto ->
                    val isFirst = index == 0
                    val isLast = index == reorderableList.lastIndex

                    EditorContentCard(
                        title = unitDto.name,
                        description = unitDto.description,
                        difficulty = unitDto.difficulty ?: DifficultyLevel.A1,
                        isActive = unitDto.isActive,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        isReordering = true,
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
                // MODO NORMAL: Usamos allUnitsDto (UnitDto) para disponer de dificultad y estado
                val filteredUnitsDto =
                    if (searchQuery.isBlank()) allUnitsDto.sortedBy { it.orderUnit } else allUnitsDto.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                itemsIndexed(filteredUnitsDto) { _, unitDto ->
                    EditorContentCard(
                        title = unitDto.name,
                        description = unitDto.description,
                        difficulty = unitDto.difficulty ?: DifficultyLevel.A1,
                        isActive = unitDto.isActive,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        isReordering = false,
                        onClick = { navigator.push(ExercisesOrUnitScreen(unitDto.id ?: 0)) }
                    )
                }
            }
        }
    }
}

// Nueva Card simplificada para el modo reordenamiento
@Composable
fun ReorderableUnitCard(
    name: String,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)), // Azul claro para indicar modo edición
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DragHandle, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "#$index  $name",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            Row {
                IconButton(onClick = onMoveUp, enabled = !isFirst) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Subir",
                        tint = if (isFirst) Color.LightGray else Color(0xFF003AB6)
                    )
                }
                IconButton(onClick = onMoveDown, enabled = !isLast) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Bajar",
                        tint = if (isLast) Color.LightGray else Color(0xFF003AB6)
                    )
                }
            }
        }
    }
}

@Composable
fun UnitCard(
    lessonUnit: LessonUnit,
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
                Text(
                    text = lessonUnit.emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = lessonUnit.title,
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

                        val (badgeColor, badgeTextColor, badgeLabel) = if (lessonUnit.status == UnitStatus.PUBLISHED) {
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
                        text = lessonUnit.description,
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

