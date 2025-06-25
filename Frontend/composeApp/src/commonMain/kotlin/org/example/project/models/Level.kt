package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class DifficultyLevel { A1, A2, B1, B2, C1, C2 }

@Serializable
data class Level(
    val id: Int? = null,
    val accent: Int,
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val order: Int,
    val isActive: Boolean = true,
    val createdAt: String? = null
)
