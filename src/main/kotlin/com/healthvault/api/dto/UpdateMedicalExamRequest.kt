package com.healthvault.api.dto

import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UpdateMedicalExamRequest(
    @field:PastOrPresent
    val performedAt: LocalDate?,

    @field:Size(max = 120)
    val requestingDoctor: String?,

    @field:Size(max = 4000)
    val result: String?,
)
