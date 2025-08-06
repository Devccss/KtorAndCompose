package com.example.dtos


import models.DifficultyLevel
import kotlinx.serialization.Serializable


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

@Serializable
data class DialogDetailDTO(
    val dialog: DialogDTOs,
    val participants: List<ParticipantDetailDTO>
)
