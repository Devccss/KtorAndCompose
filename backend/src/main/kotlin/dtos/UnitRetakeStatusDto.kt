package com.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class UnitRetakeStatusDto(
    val shouldRetake: Boolean,
    val reason: String?, // "low_score", "expired", "no_test_taken", null si no debe rehacer
    val lastTestScore: Int?,
    val lastTestDate: String?,
    val expirationDate: String?,
    val minutesRemaining: Int?,
    val passingScore: Int = 70 // Score mínimo para pasar
)

