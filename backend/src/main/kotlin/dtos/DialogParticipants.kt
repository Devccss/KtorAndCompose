package com.example.dtos

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

@Serializable
data class UpdateParticipantDTO(
    val name: String,
)

@Serializable
data class ParticipantDetailDTO(
    val participant: DialogParticipantDTO,
    val phrases: List<PhraseDetailDTO>,
)

