package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class DialogTestDto (
    val dialogId: Int,
    val testId : Int
)