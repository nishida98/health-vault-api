package com.healthvault.api.dto

import jakarta.validation.constraints.NotBlank

data class TokenValidationRequest(
    @field:NotBlank
    val token: String,
)
