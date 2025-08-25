package org.example.project


import org.example.project.screens.LoginScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import org.example.project.screens.admindScreens.AdminDashboard
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(InternalVoyagerApi::class)
@Composable
fun App() {

    MaterialTheme{
        Navigator(screen = AdminDashboard(
            adminName = "Deivid"
        ))
    }

}

class MainScreen : Screen {
    @Composable
    override fun Content() {

    }
}