package org.example.project.screens.studentScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.CustomTextField
import org.example.project.components.StudentAppLayout
import org.example.project.network.RepositoryProvider
import org.example.project.network.UserSession
import org.example.project.viewModel.UserViewModel

class StudentMeUserScreen(
    private val userIdArg: Int? = null,
    private val studentNameArg: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userVm = rememberScreenModel {
            UserViewModel(RepositoryProvider.userRepo, RepositoryProvider.unitRepo)
        }
        val userUi by userVm.state.collectAsState()

        val userId = userIdArg ?: UserSession.idUser
        val displayName = studentNameArg ?: UserSession.name ?: "Estudiante"
        val snackbarHostState = remember { SnackbarHostState() }

        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var preferences by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        LaunchedEffect(userId) {
            if (userId != null && userId > 0) {
                userVm.getUserById(userId)
            }
        }

        LaunchedEffect(userUi.currentUser?.id) {
            val user = userUi.currentUser ?: return@LaunchedEffect
            name = user.name
            email = user.email
            preferences = user.preferences.orEmpty()
        }

        LaunchedEffect(userUi.error) {
            userUi.error?.let { snackbarHostState.showSnackbar(it) }
        }

        StudentAppLayout(
            actualScreen = "Mi perfil",
            selectedIndex = -1,
            initialUserName = displayName,
            snackbarHostState = snackbarHostState
        ) { _: PaddingValues, _, _ ->
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                            Text(
                                text = "Editar informacion",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CustomTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Nombre",
                                modifier = Modifier.fillMaxWidth()
                            )
                            CustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                modifier = Modifier.fillMaxWidth()
                            )
                            CustomTextField(
                                value = preferences,
                                onValueChange = { preferences = it },
                                label = "Preferencias",
                                placeholderText = "Ejemplo: listening, speaking",
                                modifier = Modifier.fillMaxWidth()
                            )
                            CustomTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "Nueva contrasena (opcional)",
                                placeholderText = "Deja vacio para mantener la actual",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        val currentUser = userUi.currentUser
                        val canSave = currentUser != null && name.isNotBlank() && email.isNotBlank()

                        Button(
                            onClick = {
                                val safeUser = currentUser ?: return@Button
                                val safeId = safeUser.id ?: return@Button

                                userVm.updateUser(
                                    safeId,
                                    safeUser.copy(
                                        name = name.trim(),
                                        email = email.trim(),
                                        preferences = preferences.trim().ifBlank { null },
                                        password = newPassword.takeIf { it.isNotBlank() } ?: safeUser.password
                                    )
                                )

                                UserSession.set(
                                    id = safeId,
                                    name = name.trim(),
                                    role = safeUser.role,
                                    actualUnit = safeUser.currentUnitId
                                )
                                newPassword = ""
                            },
                            enabled = canSave && !userUi.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (userUi.isLoading) "Guardando..." else "Guardar cambios")
                        }
                    }
                }
            }
        }
    }
}

