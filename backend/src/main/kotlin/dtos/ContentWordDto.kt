package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ContentWordDto(
    val id: Int,
    val contentId: Int,
    val wordId: Int
)

@Serializable
data class CreateContentWordDto(
    val contentId: Int,
    val wordId: Int
)

@Serializable
data class UpdateContentWordDto(
    val contentId: Int? = null,
    val wordId: Int? = null
)
