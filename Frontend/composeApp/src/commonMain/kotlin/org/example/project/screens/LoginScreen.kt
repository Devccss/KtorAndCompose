package org.example.project.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow


class LoginScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navigator.push(StudentDashboard()) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Iniciar sesión como alumno")
            }

            Button(
                onClick = { navigator.push(AdminDashboard("Deivid")) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Iniciar sesión como administrador")
            }
        }
    }
}
