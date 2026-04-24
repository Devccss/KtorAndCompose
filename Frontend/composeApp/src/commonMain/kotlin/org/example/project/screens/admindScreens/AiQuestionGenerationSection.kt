package org.example.project.screens.admindScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.dtos.AiGeneratedQuestionDto
import org.example.project.viewModel.AiQuestionGenerationViewModel

@Composable
fun AiQuestionGenerationSection(
    contentId: Int?,
    aiVm: AiQuestionGenerationViewModel,
    onQuestionConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aiUi by aiVm.state.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "IA para generar preguntas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = "Genera propuestas a partir del contenido del ejercicio y confirma solo las que quieras guardar.",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            OutlinedTextField(
                value = aiUi.questionCountInput,
                onValueChange = aiVm::updateQuestionCount,
                label = { Text("Cantidad de preguntas") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !aiUi.isLoading,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = aiUi.language,
                    onValueChange = aiVm::updateLanguage,
                    label = { Text("Idioma") },
                    modifier = Modifier.weight(1f),
                    enabled = !aiUi.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                OutlinedTextField(
                    value = aiUi.difficulty,
                    onValueChange = aiVm::updateDifficulty,
                    label = { Text("Dificultad") },
                    modifier = Modifier.weight(1f),
                    enabled = !aiUi.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }

            Button(
                onClick = { contentId?.let { aiVm.generateQuestions(it) } },
                enabled = contentId != null && !aiUi.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (aiUi.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Generando...")
                } else {
                    Text("Generar preguntas con IA")
                }
            }

            if (contentId == null) {
                Text(
                    text = "Crea o carga el contenido del ejercicio antes de usar IA.",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309)
                )
            }

            aiUi.lastResponse?.let { response ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF166534)) {
                        Text("Aceptadas: ${response.accepted}")
                    }
                    Badge(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFF991B1B)) {
                        Text("Rechazadas: ${response.rejected.size}")
                    }
                }
            }

            if (aiUi.generatedQuestions.isEmpty() && !aiUi.isLoading) {
                Text(
                    text = "Aún no hay preguntas generadas.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    aiUi.generatedQuestions.forEachIndexed { index, question ->
                        AiGeneratedQuestionCard(
                            question = question,
                            index = index + 1,
                            enabled = contentId != null && !aiUi.isLoading,
                            onConfirm = {
                                contentId?.let {
                                    aiVm.confirmQuestion(it, question) {
                                        onQuestionConfirmed()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiGeneratedQuestionCard(
    question: AiGeneratedQuestionDto,
    index: Int,
    enabled: Boolean,
    onConfirm: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sugerencia #$index",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onConfirm,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Confirmar")
                }
            }

            Text(
                text = question.questionText,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.alternatives.forEach { alt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(
                            containerColor = if (alt.isCorrect) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                            contentColor = if (alt.isCorrect) Color(0xFF065F46) else Color(0xFF4B5563)
                        ) {
                            Text(if (alt.isCorrect) "Correcta" else "Opción")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = alt.text,
                            color = Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }
}

