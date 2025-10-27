package com.example.dtos

import kotlinx.serialization.Serializable
import models.TestType

@Serializable
data class TestDto (
    val id: Int? = null,
    val name: String,
    val description: String,
    val testType: TestType? = TestType.TRANSLATION,
    val isActive: Boolean? = true,
    val levelId: Int,
)

@Serializable
data class CreateTestDto (
    val name: String,
    val description: String,
    val testType: TestType? = TestType.TRANSLATION,
    val isActive: Boolean? = false,
)