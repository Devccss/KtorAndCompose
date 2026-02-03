package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.screens.admindScreens.UnitStatus
import org.example.project.screens.admindScreens.LessonUnit
import org.example.project.screens.admindScreens.UnitCard
import org.example.project.screens.admindScreens.UnitsSection

/**
 * Preview de la sección completa de Unidades con datos de ejemplo.
 * Usa UnitsSection para poder iterar y ajustar visualmente en tiempo real.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFF8F0)
@Composable
fun UnitsSectionPreview() {
    val sampleUnits = listOf(
        LessonUnit(1, "Introducción a Kotlin", "Descripción breve de la unidad que explica qué aprenderás.", UnitStatus.PUBLISHED, "📚"),
        LessonUnit(2, "Sintaxis básica", "Contenido sobre variables, funciones y estructuras de control.", UnitStatus.DRAFT, "🧩"),
        LessonUnit(3, "Coroutines", "Explicación sobre concurrencia con ejemplos sencillos.", UnitStatus.PUBLISHED, "⚙️")
    )

    MaterialTheme {
        Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                UnitsSection(
                    lessonUnits = sampleUnits,
                    searchQuery = "",
                    onSearchQueryChange = {}
                )
            }
        }
    }
}

/**
 * Preview de una tarjeta de unidad individual para verificar alineamiento y desbordes.
 */

@Composable
fun UnitCardPreview() {
    val unit = LessonUnit(
        id = 42,
        title = "Unidad de prueba con título muy largo que debe truncarse correctamente",
        description = "Esta es una descripción larga de prueba para ver cómo se comporta el texto dentro de la tarjeta, debe truncarse si ocupa demasiado espacio y no romper el layout.",
        status = UnitStatus.PUBLISHED,
        emoji = "📘"
    )

    MaterialTheme {
        Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                UnitCard(lessonUnit = unit)
            }
        }
    }
}