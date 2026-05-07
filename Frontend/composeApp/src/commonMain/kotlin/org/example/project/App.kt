package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        println("[App] 🔗 Obtenida URL base: $baseUrl")
        println("[App] 📱 Plataforma: ${getPlatform().name}")
        try {
            RepositoryProvider.init(httpClient, baseUrl)
            println("[App] ✅ RepositoryProvider inicializado correctamente")
            initialized = true
        } catch (e: Exception) {
            println("[App] ❌ Error al inicializar RepositoryProvider: ${e.message}")
            e.printStackTrace()
            initialized = true // Aun así inicializamos para mostrar el error en la UI
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (initialized) {
                ResponsiveWasmContainer {
                    Navigator(screen = LoginScreen())
                }
            } else {
                // Loader o pantalla de espera
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ResponsiveWasmContainer(content: @Composable () -> Unit) {
    val platformName = remember { getPlatform().name }
    val isWasm = platformName.contains("wasm", ignoreCase = true) ||
        platformName.contains("web", ignoreCase = true)

    if (!isWasm) {
        content()
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val horizontalPadding = when {
            maxWidth >= 1500.dp -> 140.dp
            maxWidth >= 1200.dp -> 104.dp
            maxWidth >= 900.dp -> 64.dp
            else -> 20.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .widthIn(max = 1100.dp)
                    .align(Alignment.TopCenter)
            ) {
                content()
            }
        }
    }
}
