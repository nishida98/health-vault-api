package com.healthvault.api.storage

data class PresignedObjectUrl(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val expiresAt: java.time.Instant,
)

interface ObjectStorageService {
    fun createUploadUrl(objectKey: String, contentType: String): PresignedObjectUrl

    fun createDownloadUrl(objectKey: String): PresignedObjectUrl
}
