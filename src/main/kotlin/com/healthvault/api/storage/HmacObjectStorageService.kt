package com.healthvault.api.storage

import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class HmacObjectStorageService(
    private val properties: ObjectStorageProperties,
) : ObjectStorageService {
    override fun createUploadUrl(objectKey: String, contentType: String): PresignedObjectUrl {
        val expiresAt = Instant.now().plus(properties.uploadExpiration)
        return PresignedObjectUrl(
            method = "PUT",
            url = signedUrl(objectKey, "upload", expiresAt),
            headers = mapOf("Content-Type" to contentType),
            expiresAt = expiresAt,
        )
    }

    override fun createDownloadUrl(objectKey: String): PresignedObjectUrl {
        val expiresAt = Instant.now().plus(properties.downloadExpiration)
        return PresignedObjectUrl(
            method = "GET",
            url = signedUrl(objectKey, "download", expiresAt),
            headers = emptyMap(),
            expiresAt = expiresAt,
        )
    }

    private fun signedUrl(objectKey: String, operation: String, expiresAt: Instant): String {
        val expiresAtEpoch = expiresAt.epochSecond
        val signature = sign("$operation:$objectKey:$expiresAtEpoch")
        return "${properties.publicBaseUrl.trimEnd('/')}/${objectKey.encodePath()}" +
            "?operation=$operation&expires=$expiresAtEpoch&signature=$signature"
    }

    private fun sign(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(properties.signingSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(key)
        return mac.doFinal(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    private fun String.encodePath(): String {
        return split("/").joinToString("/") { segment ->
            URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20")
        }
    }
}
