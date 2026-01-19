package com.example.dtos

import kotlinx.serialization.Serializable
import models.DifficultyLevel

@Serializable
data class UnitDto(
    val id: Int,
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderUnit: Float,
    val isActive: Boolean = false,
    val createdAt: String
)

@Serializable
data class CreateUnitDto(
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderUnit: Float,
    val isActive: Boolean? = false,
    val createdAt: String
)

@Serializable
data class UpdateUnitDto(
    val difficulty: DifficultyLevel? = null,
    val name: String? = null,
    val description: String? = null,
    val orderUnit: Float? = null,
    val isActive: Boolean? = null
)
@Serializable
data class CreateUnitCompletedDto(
    val userId: Int,
    val unitId: Int,
    val completedAt: String
)

@Serializable
data class UnitCompletedDto(
    val id: Int,
    val userId: Int,
    val unitId: Int,
    val completedAt: String
)

@Serializable
data class UpdateUnitCompletedDto(
    val userId: Int? = null,
    val unitId: Int? = null,
    val completedAt: String? = null
)
