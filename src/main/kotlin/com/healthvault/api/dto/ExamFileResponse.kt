package com.healthvault.api.dto

import java.time.Instant
import java.util.UUID

data class ExamFileResponse(
    val id: UUID,
    val examId: UUID,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
    val createdAt: Instant,
)
