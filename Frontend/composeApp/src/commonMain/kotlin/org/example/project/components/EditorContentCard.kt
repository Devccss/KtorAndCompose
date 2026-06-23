package org.example.project.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.encode_sans_variable
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.example.project.dtos.DifficultyLevel
import org.jetbrains.compose.resources.Font

/**
 * Card reutilizable para Unidades, Ejercicios y Tests
 * @param title Nombre del elemento
 * @param description Descripción del elemento
 * @param difficulty Nivel de inglés (A1-C2)
 * @param isActive Estado: true = activo/publicado, false = inactivo/borrador
 * @param icon Icono a mostrar (MenuBook para unidades, Article para ejercicios, etc.)
 * @param testName Nombre del test asociado (opcional, solo para ejercicios)
 * @param isReordering Si es true, reemplaza el icono por flechas
 * @param isFirst Si es true, deshabilita la flecha hacia arriba
 * @param isLast Si es true, deshabilita la flecha hacia abajo
 * @param onMoveUp Callback para mover hacia arriba
 * @param onMoveDown Callback para mover hacia abajo
 * @param onClick Callback al hacer click en la card
 */
@Composable
fun EditorContentCard(
    title: String,
    description: String? = null,
    difficulty: DifficultyLevel? = null,
    isActive: Boolean,
    icon: ImageVector,
    testName: String? = null,
    isReordering: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val encodeSansFamily = FontFamily(Font(Res.font.encode_sans_variable))
    val jetbrainsMonoFamily = FontFamily(Font(Res.font.jetbrains_mono_regular))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = !isReordering && onClick != null) { onClick?.invoke() }
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReordering) Color(0xFFE8F0FE) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono o flechas (lado izquierdo)
            if (isReordering) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    IconButton(
                        onClick = { onMoveUp?.invoke() },
                        enabled = !isFirst,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Subir",
                            tint = if (isFirst) Color.LightGray else Color(0xFFD5D5D5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMoveDown?.invoke() },
                        enabled = !isLast,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Bajar",
                            tint = if (isLast) Color.LightGray else Color(0xFFD5D5D5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF003AB6),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido principal (centro)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título + Círculo de estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D),
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = encodeSansFamily,
                    )

                    difficulty?.let {
                        Badge(
                            containerColor = difficultyBadgeBackgroundColor(difficulty),
                            contentColor = difficultyBadgeTextColor(difficulty),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                difficulty.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                    }

                    // Círculo de estado (pequeño)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .shadow(0.5.dp, shape = CircleShape)
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        ) {}
                    }
                }

                // Descripción
                description?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = jetbrainsMonoFamily
                    )
                }

                // Nivel de inglés + Test (si aplica)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    // Nombre del test asociado (solo para ejercicios)
                    testName?.let {
                        Badge(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFFD5D5D5),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                it,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun difficultyBadgeBackgroundColor(difficulty: DifficultyLevel): Color {
    return when (difficulty) {
        DifficultyLevel.A1 -> Color(0xFFE0E0E0)
        DifficultyLevel.A2 -> Color(0xFFF2D8C2)
        DifficultyLevel.B1 -> Color(0xFFF6C99C)
        DifficultyLevel.B2 -> Color(0xFFF4B26B)
        DifficultyLevel.C1 -> Color(0xFFF2953D)
        DifficultyLevel.C2 -> Color(0xFFE86E1D)
    }
}

@Composable
private fun difficultyBadgeTextColor(difficulty: DifficultyLevel): Color {
    return when (difficulty) {
        DifficultyLevel.A1 -> Color(0xFF4A4A4A)
        DifficultyLevel.A2 -> Color(0xFF7A5A3A)
        DifficultyLevel.B1 -> Color(0xFF8A4F13)
        DifficultyLevel.B2 -> Color(0xFF8F4600)
        DifficultyLevel.C1 -> Color(0xFFFFFFFF)
        DifficultyLevel.C2 -> Color(0xFFFFFFFF)
    }
}

