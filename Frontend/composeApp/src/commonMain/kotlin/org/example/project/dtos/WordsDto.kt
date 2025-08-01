package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class WordDto(
    val id: Int,
    val english: String,
    val spanish: String,
    val phonetic: String? = null,
    val description: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateWordDto(
    val english: String,
    val spanish: String,
    val phonetic: String? = null,
    val description: String? = null,
)
