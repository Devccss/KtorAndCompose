package com.example.dtos

import DifficultyLevel
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

@Serializable
data class DialogDTOs(
    val id: Int,
    val levelId: Int,
    val name: String,
    val difficulty: DifficultyLevel,
    val description: String? = null,
    val audioUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class CreateDialogDTO(
    val levelId: Int,
    val name: String,
    val difficulty: DifficultyLevel,
    val description: String,
    val audioUrl: String? = null
)

@Serializable
data class UpdateDialogDTO(
    val levelId: Int? = null,
    val name: String? = null,
    val difficulty: DifficultyLevel? = null,
    val description: String? = null,
    val audioUrl: String? = null,
    val isActive: Boolean? = null
)


