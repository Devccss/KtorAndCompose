package org.example.project.dtos

import kotlinx.serialization.Serializable

enum class DifficultyLevel{ A1, A2, B1, B2, C1, C2 }

@Serializable
data class UnitDto(
    val id: Int? = null,
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
    var isActive: Boolean? = null
)
@Serializable
data class CreateUnitCompletedDto(
    val userId: Int,
    val unitId: Int,
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

@Serializable
data class FilterUnitsDto(
    val name: String? = null,
    val difficulty: DifficultyLevel? = null,
    val isActive: Boolean? = null
)