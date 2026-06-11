package com.healthvault.api.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "health-vault.jwt")
data class JwtProperties(
    val secret: String,
    val expirationMinutes: Long,
)
