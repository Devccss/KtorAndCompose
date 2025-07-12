
package org.example.project.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import org.example.project.models.Level


class LevelDetails(
    val levelId: Int?,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        var level by remember { mutableStateOf<Level?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(levelId) {
            if (levelId != null) {
                try {
                    isLoading = true
                    level = RepositoryProvider.levelRepository.getLevelById(levelId)
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        }

        Column {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text("Error: $error")
                level != null -> {
                    Text("Detalles del nivel")
                    Text("ID: ${level!!.id}")
                    Text("Nombre: ${level!!.name}")
                    Text("Descripción: ${level!!.description}")
                    Text("Dificultad: ${level!!.difficulty}")
                }
                else -> Text("Nivel no encontrado")
            }
        }
    }
}