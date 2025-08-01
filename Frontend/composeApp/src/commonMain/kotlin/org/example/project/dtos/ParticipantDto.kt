package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class DialogParticipantDTO(
    val id: Int,
    val dialogId: Int,
    val name: String,
    val createdAt: String? = null
)

@Serializable
data class CreateParticipantDTO (
    val name : String,
)