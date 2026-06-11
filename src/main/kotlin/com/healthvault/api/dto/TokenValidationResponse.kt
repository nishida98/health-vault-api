package com.healthvault.api.dto

import java.time.Instant
import java.util.UUID

data class TokenValidationResponse(
    val valid: Boolean,
    val userId: UUID,
    val email: String,
    val expiresAt: Instant,
)
