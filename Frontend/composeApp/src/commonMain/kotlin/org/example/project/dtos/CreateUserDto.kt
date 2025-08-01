package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDto (
    val name: String,
    val email: String,
    val password: String,
    val preferences: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val currentLevelId: Int? = null
)