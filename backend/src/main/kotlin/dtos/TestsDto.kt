package com.example.dtos

import kotlinx.serialization.Serializable
import models.ContentType

@Serializable
data class TestDto(
    val id: Int,
    val unitId: Int,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = false,
    val createdAt: String
)

@Serializable
data class CreateTestDto(
    val unitId: Int,
    val name: String,
    val description: String? = null,
    val isActive: Boolean? = false,
    val createdAt: String
)

@Serializable
data class UpdateTestDto(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class TestCompletedDto(
    val id: Int,
    val userId: Int,
    val testId: Int,
    val completedAt: String
)

@Serializable
data class CreateTestCompletedDto(
    val userId: Int,
    val testId: Int,
    val completedAt: String
)

@Serializable
data class UpdateTestCompletedDto(
    val userId: Int? = null,
    val testId: Int? = null,
    val completedAt: String? = null
)
