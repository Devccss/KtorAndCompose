package org.example.project.screens.admindScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.example.project.components.AppLayout
import org.example.project.components.NavItem
import org.example.project.components.ReusableBottomBar
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
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

        val lessonUnits = unitUi.unit.map { u ->
            LessonUnit(
                id = u.id ?: 0,
                title = u.name,
                description = u.description ?: "",
                status = if (u.isActive) UnitStatus.PUBLISHED else UnitStatus.DRAFT,
                emoji = "📚"
            )
        }

        var searchQuery by remember { mutableStateOf("") }
        LaunchedEffect(unitUi.error) {
            unitUi.error?.let {
                snackbarHostState.showSnackbar(it)
            }
        }


        AppLayout(
            actualScreen = "Administrar Unidades",
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

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Usamos la UnitsSection tal como la pediste
                UnitsSection(
                    navigator = navigator,
                    lessonUnits = lessonUnits,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
        }
    }
}

// Implementación de UnitsSection (según tu especificación)
@Composable
fun UnitsSection(
    navigator: Navigator,
    lessonUnits: List<LessonUnit>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Barra de búsqueda y filtro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
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

        // Botón de agregar nueva unidad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { /* Agregar nueva unidad */ },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8F4C4)
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "+ Agregar nueva unidad",
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

        // Lista de unidades
        lessonUnits.forEach { unit ->
            UnitCard(lessonUnit = unit, onClick = {navigator.push(UnitsDetailsScreen(unit.id)) })
        }
    }
}

@Composable
fun UnitCard(lessonUnit: LessonUnit, onClick: () -> Unit = {}) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji
                    Text(
                        text = lessonUnit.emoji,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Título con peso para evitar que la badge lo comprima
                    Text(
                        text = lessonUnit.title,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontFamily = encodeSansFamily
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Badge (anchura mínima para mantener espacio)
                    val (badgeColor, badgeTextColor, badgeLabel) = if (lessonUnit.status == UnitStatus.PUBLISHED) {
                        Triple(Color(0xFFB8F4C4), Color(0xFF2D5E3D), "Publicado")
                    } else {
                        Triple(Color(0xFFFFD4D4), Color(0xFF8B0000), "Borrador")
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 42.dp)
                            .defaultMinSize(minHeight = 10.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 8.sp,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = encodeSansFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción debajo, con límite de líneas para evitar desbordes
                Text(
                    text = lessonUnit.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = jetbrainsMonoFamily // <-- descripción con JetBrains Mono
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Columna de acciones (botones) con ancho ajustado
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                // Usar defaultMinSize para forzar mismo tamaño mínimo en ambos botones
                val actionButtonModifier = Modifier
                    .defaultMinSize(minWidth = 92.dp, minHeight = 36.dp)


                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { /* Eliminar */ },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A4A4A)
                    ),
                    modifier = actionButtonModifier
                ) {
                    Text(
                        text = "Ver",
                        fontSize = 13.sp,
                        fontFamily = jetbrainsMonoFamily
                    )
                }
            }
        }
    }
}