package com.healthvault.api.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MoveMedicalExamRequest(
    @field:NotNull
    val folderId: UUID,
)
