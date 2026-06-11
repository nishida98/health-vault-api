package com.healthvault.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class CreateMedicalExamRequest(
    @field:PastOrPresent
    val performedAt: LocalDate,

    @field:NotBlank
    @field:Size(max = 120)
    val requestingDoctor: String,

    @field:NotBlank
    @field:Size(max = 120)
    val examType: String,

    @field:NotBlank
    @field:Size(max = 4000)
    val result: String,

    val folderId: UUID,
)
