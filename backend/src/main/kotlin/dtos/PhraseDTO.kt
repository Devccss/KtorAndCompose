package com.example.dtos
import kotlinx.serialization.Serializable


@Serializable
data class PhraseDto(
    val id: Int,
    val participantId: Int,
    val audioUrl: String? =null,
    val englishText: String,
    val spanishText: List<String>? = emptyList(),
    val isActive: Boolean,
    val createdAt: String? = null
)

@Serializable
data class CreatePhraseDto (
    val audioUrl: String? =null,
    val englishText : String,
    val spanishText: List<String>? = emptyList()
)

@Serializable
data class OrderPhraseDto (
    val dialogId: Int,
    val phraseId: Int,
    val order: Int
)

@Serializable
data class PhraseDetailDTO(
    val phrase: PhraseDto,
    val words: List<WordDto>,
    val oreder: Int? = null
)