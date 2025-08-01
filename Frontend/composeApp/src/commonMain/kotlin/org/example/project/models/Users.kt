package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class Users (
    val id: Int? = null,
    val name: String,
    val email: String,
    val preferences: String? = null,
    val password: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val currentLevelId: Int? = null,
    val createdAt: String? = null,
    val role: Role? = Role.STUDENT
)

enum class Role { ADMIN, CONTENT_EDITOR, STUDENT }