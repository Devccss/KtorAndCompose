package org.example.project.screens.admindScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class UsersScreen : Screen {
    override val key = uniqueScreenKey
    @Composable
    override fun Content() {
        Column { Text("Pantalla administar Usuarios") }
    }
}