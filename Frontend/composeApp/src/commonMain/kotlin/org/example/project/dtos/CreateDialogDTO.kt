package org.example.project.dtos

import kotlinx.serialization.Serializable
import org.example.project.models.DifficultyLevel

@Serializable
data class CreateDialogDTO(
    val levelId: Int,
    val name: String,
    val difficulty: DifficultyLevel,
    val description: String,
    val audioUrl: String? = null
)