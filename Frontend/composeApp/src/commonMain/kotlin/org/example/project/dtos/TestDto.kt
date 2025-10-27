package org.example.project.dtos

import kotlinx.serialization.Serializable
import org.example.project.models.TestType

@Serializable
data class CreateTest (
    val name: String,
    val description: String,
    val testType: TestType? = TestType.TRANSLATION,
    val isActive: Boolean? = false,
)