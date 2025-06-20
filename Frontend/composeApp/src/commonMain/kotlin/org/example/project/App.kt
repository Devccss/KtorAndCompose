package org.example.project


import org.example.project.Screens.LoginScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(InternalVoyagerApi::class)
@Composable
@Preview
fun App() {

    MaterialTheme{
        Navigator(screen = LoginScreen())
    }

}

class MainScreen : Screen {
    @Composable
    override fun Content() {

    }
}