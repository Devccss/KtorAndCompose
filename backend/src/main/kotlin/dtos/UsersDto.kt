package com.example.dtos
import androidx.room.RoomKspProcessor
import models.Role
import kotlinx.serialization.Serializable

@Serializable
data class UsersDto(
    val id: Int,
    val name: String,
    val email: String,
    val preferences: String? = null,
    val password: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val currentLevelId: Int? = null,
    val createdAt: String,
    val role: Role
)
@Serializable
data class CreateUserDto(
    val name: String,
    val email: String,
    val password: String,
    val preferences: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val currentLevelId: Int? = null,
    val role: Role? = Role.STUDENT
)

@Serializable
data class GoogleUserDto(
    val name: String,
    val email: String,
    val preferences: String? = null,
    val provider: String? = "google",
    val providerId: String? = null,
    val currentLevelId: Int? = null


)

@Serializable
data class LoginDto(
    val email: String,
    val password: String
)