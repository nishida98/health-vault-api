package com.healthvault.api.dto

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class MedicalExamResponse(
    val id: UUID,
    val userId: UUID,
    val performedAt: LocalDate,
    val requestingDoctor: String,
    val result: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
