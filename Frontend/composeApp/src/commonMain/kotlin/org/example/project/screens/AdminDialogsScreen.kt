package org.example.project.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class AdminDialogsScreen : Screen {
    override val key = uniqueScreenKey
    @Composable
    override fun Content() {
        Column { Text("Pantalla administar Dialogos") }
    }
}