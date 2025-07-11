package org.example.project.dtos

import kotlinx.serialization.Serializable
import org.example.project.models.DifficultyLevel

@Serializable
data class LevelCreationDTO(
    val name: String,
    val description: String,
    val difficulty: DifficultyLevel
)