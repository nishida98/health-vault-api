package com.healthvault.api.dto

import java.time.Instant

data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresAt: Instant,
    val user: UserAccountResponse,
)
