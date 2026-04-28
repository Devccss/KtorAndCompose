package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.example.project.network.RepositoryProvider
import org.example.project.network.createHttpClient
import org.example.project.screens.LoginScreen



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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (initialized) {
                Navigator(screen = LoginScreen(userId = 2))
            } else {
                // Loader o pantalla de espera
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}