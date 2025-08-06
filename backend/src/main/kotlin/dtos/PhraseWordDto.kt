package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PhraseWordDto(
    val id: Int? = null,
    val phraseId: Int,
    val wordId: Int,
    val select: Boolean? = false,
    val order : Int
)

@Serializable
data class CreatePhraseWordDto(
    val phraseId: Int,
    val wordId: Int,
    val order : Int
)