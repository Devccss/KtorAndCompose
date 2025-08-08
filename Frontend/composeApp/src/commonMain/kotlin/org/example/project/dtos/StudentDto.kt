package org.example.project.dtos

import kotlinx.serialization.Serializable

@Serializable
data class StudentDto (
    val id : Int,
    val userId : Int,
    val completedDialogs: Int,
    val totalDialogs : Int,
    val testScore : Int? = null,
    val isLevelCompleted : Boolean = false,
    val lastAccessed : String? = null,
)
@Serializable
data class ProgressDto(
    val id : Int? = null,
    val completedDialogs: Int,
    val totalDialogs: Int,
    val testScore: Int? = null,
    val lastAccessed: String? = null,
)