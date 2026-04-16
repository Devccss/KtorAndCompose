package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.example.project.dtos.Role
import org.example.project.network.RepositoryProvider
import org.example.project.network.createHttpClient
import org.example.project.screens.LoginScreen
import org.example.project.screens.admindScreens.AdminDashboard
import org.example.project.screens.studentScreens.StudentWelcomeScreen


@Composable
fun App() {
    val httpClient = remember { createHttpClient() }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val baseUrl = getBaseUrl()
        RepositoryProvider.init(httpClient, baseUrl)
        initialized = true
    }

    MaterialTheme {
        if (initialized) {
            //Navigator(screen = AdminDashboard(1,"Deivid",Role.ADMIN))
            //Navigator(screen = LoginScreen())
            Navigator(screen = StudentWelcomeScreen( 4))
        } else {
            // Loader o pantalla de espera
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}