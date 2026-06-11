package com.healthvault.api.dto

import java.time.Instant
import java.util.UUID

data class ExamFolderResponse(
    val id: UUID,
    val userId: UUID,
    val parentId: UUID?,
    val name: String,
    val depth: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
