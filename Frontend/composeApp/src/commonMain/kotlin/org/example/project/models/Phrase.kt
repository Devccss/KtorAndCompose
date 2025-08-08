package org.example.project.models

import kotlinx.serialization.Serializable


@Serializable
data class Phrase(
    val id: Int,
    val participantId: Int,
    val audioUrl: String? =null,
    val englishText: String,
    val spanishText: List<String>? = emptyList(),
    val isActive: Boolean,
    val createdAt: String? = null
)