package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val id: Int? = null,
    val dialogId : Int,
    val name : String,
    val createdAt : String? = null
)