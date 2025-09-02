package org.example.project.models

import kotlinx.serialization.Serializable


@Serializable
data class Phrase(
    val id: Int? = null,
    val participantId: Int,
    val audioUrl: String? = null,
    val englishText: String? = null,
    val spanishText: List<String>? = emptyList(),
    val isActive: Boolean,
    val createdAt: String? = null
)