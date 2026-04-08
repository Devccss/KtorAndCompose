package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class WelcomeTestDto(
    val testId : Int,
    val isActive : Boolean,
    val createAt : String
)
@Serializable
data class CreateWelcomeTestDto(
    val testId: Int,
    val isActive : Boolean,
)
@Serializable
data class UpdateWelcomeTestDto(
    val testId: Int? = null,
    val isActive : Boolean? = null,
)