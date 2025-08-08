package org.example.project.dtos

import kotlinx.serialization.Serializable
import org.example.project.models.Dialog

@Serializable
data class DialogDetailDTO(
    val dialog: Dialog,
    val participants: List<ParticipantDetailDto>
)