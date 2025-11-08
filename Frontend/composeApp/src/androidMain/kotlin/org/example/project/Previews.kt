package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.dtos.DialogParticipantDTO
import org.example.project.dtos.PhraseDto
import org.example.project.models.Dialog
import org.example.project.models.DifficultyLevel
import org.example.project.models.Level
import org.example.project.models.Phrase
import org.example.project.models.Role
import org.example.project.models.TestType
import org.example.project.models.Users
import org.example.project.screens.admindScreens.DialogDetails
import org.example.project.screens.admindScreens.EditUser
import org.example.project.viewModel.LevelsUiState
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import org.jetbrains.compose.ui.tooling.preview.Preview


val listOfLevels: List<Level> = listOf(
    Level(
        id = 1,
        accent = 1,
        difficulty = DifficultyLevel.A1,
        name = "Nivel 1",
        description = "Descripcion nivel 1",
        orderLevel = 1.0f,
        isActive = true,
        createdAt = "2025-08-08T10:59:04.922732"
    ),
    Level(
        id = 2,
        accent = 2,
        difficulty = DifficultyLevel.A2,
        name = "Nivel 2",
        description = "Descripcion nivel 2",
        orderLevel = 2.0f,
        isActive = true,
        createdAt = "2025-08-08T10:59:04.922732"
    )
)

@Preview()
@Composable
fun AppAndroidPreview() {
    EditUserPreview(
        initial = Users(
            name = "",
            email = "",
            password = "Hola",
            role = null,
            currentLevelId = listOfLevels[0].id
        ),
        levels = listOfLevels,
        onSave = { newUser ->

            var showAddUser = false

        },
        onDismiss = { var showAddUser = false }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserPreview(
    initial: Users,
    levels: List<Level>,
    onSave: (Users) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var email by remember { mutableStateOf(initial.email) }
    val password by remember { mutableStateOf(initial.password?.isNotEmpty()) }
    var newPassword by remember { mutableStateOf("") }
    var changePassword by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val roles by remember { mutableStateOf(Role.entries) }
    var role by remember { mutableStateOf(initial.role) }
    var roleExpanded by remember { mutableStateOf(false) }
    var selectedLevel = levels.find { it.id == initial.currentLevelId }

    val testTypes = TestType.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })

                OutlinedTextField(email, { email = it }, label = { Text("Email") })

                if (selectedLevel != null) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLevel?.name ?: "Selecciona un nivel",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nivel") },
                            modifier = Modifier.menuAnchor(
                                MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ).fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            levels.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = {
                                        selectedLevel = level
                                        expanded = false
                                    }
                                )
                            }
                        }

                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,

                    ) {

                    if(password?.and(!changePassword) == true){
                        Card() {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Contraseña")
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    Modifier
                                        .background(
                                            Color(0xFF60D16D)
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        "Asignada",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }


                        TextButton(
                            onClick = { changePassword = !changePassword },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                "Cambiar" ,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }else{
                        OutlinedTextField(newPassword, { newPassword = it }, label = { Text("Nueva Contraseña") })
                    }


                }

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role?.name ?: roles[2].name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.name) },
                                onClick = {
                                    role = rol
                                    roleExpanded = false
                                }
                            )
                        }
                    }

                }

            }
        },
        confirmButton = {
            TextButton(onClick = {

                if (name.isNotBlank()) {
                    onSave(
                        initial.copy(
                            name = name,
                            email = email,
                            provider = null,
                            providerId = null,
                            preferences = null,
                            role = role,
                            currentLevelId = selectedLevel?.id,


                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


