package com.healthvault.api.dto

import java.time.Instant
import java.util.UUID

data class PresignedFileUrlResponse(
    val file: ExamFileResponse,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val expiresAt: Instant,
)
