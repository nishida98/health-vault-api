package com.healthvault.api.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateExamFileUploadRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val fileName: String,

    @field:NotBlank
    @field:Size(max = 120)
    val contentType: String,

    @field:Min(1)
    @field:Max(52_428_800)
    val sizeBytes: Long,
)
