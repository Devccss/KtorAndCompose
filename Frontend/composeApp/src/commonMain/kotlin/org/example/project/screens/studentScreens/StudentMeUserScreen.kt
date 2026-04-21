package org.example.project.screens.studentScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
        var newPassword by remember { mutableStateOf("") }
        val currentUser = userUi.currentUser
        val canSave = currentUser != null && name.isNotBlank() && email.isNotBlank()

        LaunchedEffect(userId) {
            if (userId != null && userId > 0) {
                userVm.getUserById(userId)
            }
        }

        LaunchedEffect(userUi.currentUser?.id) {
            val user = userUi.currentUser ?: return@LaunchedEffect
            name = user.name
            email = user.email
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
                        .padding(16.dp),
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
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = "Editar perfil",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Actualiza solo tu nombre, email y contraseña",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D5E3D))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(56.dp)
                                            .padding(horizontal = 18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (name.firstOrNull() ?: displayName.firstOrNull() ?: "U").toString().uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name.ifBlank { displayName },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = email.ifBlank { "correo@ejemplo.com" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Información básica",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Nombre") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Seguridad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Nueva contraseña") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    placeholder = { Text("Opcional") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Text(
                                    text = "Déjalo vacío si no quieres cambiar tu contraseña.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val safeUser = currentUser ?: return@Button
                                val safeId = safeUser.id ?: return@Button

                                userVm.updateUser(
                                    safeId,
                                    safeUser.copy(
                                        name = name.trim(),
                                        email = email.trim(),
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5E3D))
                        ) {
                            Text(
                                if (userUi.isLoading) "Guardando..." else "Guardar cambios",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

