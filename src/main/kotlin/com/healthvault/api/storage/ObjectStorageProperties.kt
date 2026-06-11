package com.healthvault.api.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "health-vault.storage")
data class ObjectStorageProperties(
    val publicBaseUrl: String = "http://localhost:9000/health-vault",
    val signingSecret: String = "health-vault-local-storage-secret",
    val uploadExpiration: Duration = Duration.ofMinutes(15),
    val downloadExpiration: Duration = Duration.ofMinutes(5),
)
