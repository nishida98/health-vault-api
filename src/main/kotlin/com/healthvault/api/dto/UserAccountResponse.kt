package com.healthvault.api.dto

import java.time.Instant
import java.util.UUID

data class UserAccountResponse(
    val id: UUID,
    val name: String,
    val nickname: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
