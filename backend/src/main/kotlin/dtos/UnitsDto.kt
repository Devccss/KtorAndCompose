package com.example.dtos

import kotlinx.serialization.Serializable
import models.DifficultyLevel

@Serializable
data class UnitDto(
    val id: Int,
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderUnit: Int,
    val isActive: Boolean = false,
    val createdAt: String
)

@Serializable
data class CreateUnitDto(
    val difficulty: DifficultyLevel,
    val name: String,
    val description: String,
    val orderUnit: Int? = null,
    val isActive: Boolean? = false,
    val createdAt: String? = null
)

@Serializable
data class UpdateUnitDto(
    val difficulty: DifficultyLevel? = null,
    val name: String? = null,
    val description: String? = null,
    val orderUnit: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class FilterUnitsDto(
    val name: String? = null,
    val difficulty: DifficultyLevel? = null,
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

