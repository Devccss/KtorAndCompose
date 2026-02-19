package org.example.project.screens.admindScreens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import cafe.adriel.voyager.navigator.Navigator
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.components.AppLayout
import org.example.project.dtos.UnitDto
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.ExercisesViewModel
import org.example.project.viewModel.UnitViewModel
import org.jetbrains.compose.resources.Font

class ExercisesOrUnitScreen(private val unitId: Int? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val exerciseVm =
            rememberScreenModel { ExercisesViewModel(RepositoryProvider.exerciseRepo, unitId) }
        val unitVm = rememberScreenModel { UnitViewModel(RepositoryProvider.unitRepo, unitId) }
        val unitUi by unitVm.state.collectAsState()
        val exerciseUi by exerciseVm.state.collectAsState()

        var selectedIndex by remember { mutableStateOf(3) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(exerciseUi.error) {
            exerciseUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
        LaunchedEffect(Unit) {
            unitVm.refreshUnits()
        }

        // Mapear ejercicios a modelo UI local
        val exercises = exerciseUi.exercise.map { ex ->
            ExerciseItem(
                id = ex.id,
                unitId = ex.unitId,
                name = ex.name,
                description = ex.description ?: "",
                isActive = ex.isActive
            )
        }

        var searchQuery by remember { mutableStateOf("") }

        AppLayout(
            actualScreen = "Administrar Unidad",
            selectedIndex = selectedIndex,
            onSelect = { idx -> selectedIndex = idx },
            initialUserName = UserSession.name,
            role = UserSession.role,
            snackbarHostState = snackbarHostState
        ) { _, _, _ ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExercisesSection(
                    navigator = navigator,
                    actualUnit = unitUi.actualUnit,
                    showUnitHeader = unitId != null, // Nuevo parámetro: solo true si unitId existe
                    exercises = exercises,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onAdd = {
                        // Navegar a pantalla de crear ejercicio si existe, o abrir diálogo
                        // navigator.push(CreateExerciseScreen(unitId ?: 0))
                    },

                    )
            }
        }

    }
}

// Modelo UI local para renderizar
data class ExerciseItem(
    val id: Int,
    val unitId: Int,
    val name: String,
    val description: String,
    val isActive: Boolean
)

// Section que contiene búsqueda, botones y lista de ejercicios
@Composable
fun ExercisesSection(
    navigator: Navigator,
    actualUnit: UnitDto? = null,
    showUnitHeader: Boolean = false, // Parámetro con valor por defecto
    exercises: List<ExerciseItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

    Card(
        // Agrego colores para mantener consistencia
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
            // Barra de búsqueda y filtro
            // Verificamos ambas condiciones: que se deba mostrar Y que existan datos
            if (showUnitHeader && actualUnit != null) {

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row {
                                Text(
                                    text = actualUnit.name,
                                    fontFamily = encodeSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF131313)
                                )
                                // Badge de estado
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (actualUnit.isActive) Color(0xFFE6F4EA) else Color(
                                                0xFFFFF4E5
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (actualUnit.isActive) "Activo" else "Inactivo",
                                        color = if (actualUnit.isActive) Color(0xFF1E7E34) else Color(
                                            0xFFB95000
                                        ),
                                        fontSize = 12.sp,
                                        fontFamily = jetbrainsMonoFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                text = actualUnit.description,
                                fontFamily = jetbrainsMonoFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF4A4A4A),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar Unidad",
                                    tint = Color(0xFF4A4A4A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                }


                Spacer(Modifier.height(8.dp))

                Column {
                    Text("Ejercicios asociados")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar ejercicios...", fontSize = 14.sp) },
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
                    onClick = { /* Abrir filtros */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = Color(0xFF4A4A4A)
                    )
                }
            }

            // Botón de agregar nuevo ejercicio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB8F4C4)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "+ Agregar nuevo ejercicio",
                        color = Color(0xFF2D5E3D),
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { /* Ordenar */ },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A4A4A)
                    )
                ) {
                    Text("Orden")
                }
            }

            // Lista de ejercicios (aplicar búsqueda simple)
            val filtered = if (searchQuery.isBlank()) exercises else {
                exercises.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filtered.isEmpty()) {
                Text("No hay ejercicios", color = Color.Gray)
            } else {
                filtered.forEach { ex ->
                    ExerciseCard(
                        exercise = ex,
                        onClick = { navigator.push(ExercisesDetailsScreen(ex.id, ex.unitId)) },

                        )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseItem,
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

                Text(
                    text = exercise.description,
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