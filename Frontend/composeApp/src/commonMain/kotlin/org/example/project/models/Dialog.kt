package org.example.project.models

import kotlinx.serialization.Serializable


@Serializable
data class Dialog(
    val id: Int? = null,
    val levelId: Int,
    val name: String,
    val difficulty: DifficultyLevel,
    val description: String,
    val audioUrl: String,
    val isActive: Boolean? = true,
    val createdAt: String? = null
)