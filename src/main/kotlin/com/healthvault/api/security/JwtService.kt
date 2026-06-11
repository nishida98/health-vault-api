package com.healthvault.api.security

import com.healthvault.api.entity.UserAccount
import com.healthvault.api.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    private val properties: JwtProperties,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray())

    fun createToken(user: UserAccount): TokenDetails {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plus(properties.expirationMinutes, ChronoUnit.MINUTES)
        val token = Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact()

        return TokenDetails(token = token, expiresAt = expiresAt)
    }

    fun validate(token: String): ValidatedToken {
        val claims = parseClaims(token)

        return ValidatedToken(
            userId = UUID.fromString(claims.subject),
            email = claims["email", String::class.java],
            expiresAt = claims.expiration.toInstant(),
        )
    }

    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (exception: Exception) {
            throw InvalidTokenException()
        }
    }
}

data class TokenDetails(
    val token: String,
    val expiresAt: Instant,
)

data class ValidatedToken(
    val userId: UUID,
    val email: String,
    val expiresAt: Instant,
)
