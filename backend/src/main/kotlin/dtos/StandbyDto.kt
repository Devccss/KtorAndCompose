package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class StandbyDto (
    val id: Int,
    val userId: Int,
    val phraseId : Int,
    val incorrectAttempts : Int = 0,
    val addedAt : String? = null
)

@Serializable
data class StandbyUpdateDto (
    val incorrectAttempts : Int? = null,
    val addedAt : String? = null
)