package org.example.project.dtos

import kotlinx.serialization.Serializable

enum class Role {
    ADMIN,
    STUDENT,
    CONTENT_EDITOR
}

@Serializable
data class UserDto(
    val id: Int? = null,
    val email: String,
    val name: String,
    val password: String? = null,
    val provider: String? = null,
    val preferences: String? = null,
    val activeNow: Boolean? = false,
    val currentUnitId: Int? = null,
    val createdAt: String,
    val role: Role? = Role.STUDENT
)
@Serializable
data class CreateUserDto(
    val name: String,
    val email: String,
    val password: String,
    val preferences: String? = null,
    val activeNow: Boolean? = null,
    val provider: String? = null,
    val currentUnitId: Int? = null,
    val createdAt: String? = null,
    val role: Role? = Role.STUDENT
)

@Serializable
data class LoginDto(
    val email: String,
    val password: String
)

@Serializable
data class UpdateUserDto(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val preferences: String? = null,
    val activeNow: Boolean? = null,
    val provider: String? = null,
    val currentUnitId: Int? = null,
    val role: Role? = Role.STUDENT
)