package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class DialogParticipantDTO(
    val id: Int? = null,
    val dialogId: Int? = null,
    val name: String,
    val createdAt: String? = null
)

@Serializable
data class CreateParticipantDTO (
    val name : String,
)

@Serializable
data class ParticipantDetailDto(
    val participant: DialogParticipantDTO,
    val phrases: List<PhraseDetailDto>,
)