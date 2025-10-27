package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class TestType { ALTERNATIVES, TRANSLATION, LISTENING, READING  }


@Serializable
data class Test (
    val id: Int? = null,
    val name: String,
    val description: String,
    val testType: TestType? = TestType.TRANSLATION,
    val isActive: Boolean? = true,
    val levelId: Int,
)

