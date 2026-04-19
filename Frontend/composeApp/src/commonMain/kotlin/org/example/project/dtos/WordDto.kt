package org.example.project.dtos
import kotlinx.serialization.Serializable

@Serializable
data class WordDto(
    val id: Int,
    val english: String,
    val spanish: String,
    val phonetic: String? = null,
    val description: String? = null,
    val isActive: Boolean? = false,
    val createdAt: String? = null
)

@Serializable
data class CreateWordDto(
    val english: String,
    val spanish: String,
    val phonetic: String? = null,
    val description: String? = null,
    val isActive: Boolean? = false,
)

@Serializable
data class UpdateWordDto(
    val english: String? = null,
    val spanish: String? = null,
    val phonetic: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)
@Serializable
data class FilterWordsDto(
    val english: String? = null,
    val spanish: String? = null,
    val phonetic: String? = null,
    val isActive: Boolean? = null
)