package com.healthvault.api.security

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Component
class PasswordHasher {
    private val random = SecureRandom()
    private val encoder = Base64.getEncoder()

    fun hash(password: String): PasswordHash {
        val salt = ByteArray(SALT_SIZE_BYTES)
        random.nextBytes(salt)

        return PasswordHash(
            hash = encodeHash(password, salt),
            salt = encoder.encodeToString(salt),
        )
    }

    private fun encodeHash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE_BITS)
        val bytes = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        return encoder.encodeToString(bytes)
    }

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 210_000
        private const val KEY_SIZE_BITS = 256
        private const val SALT_SIZE_BYTES = 32
    }
}

data class PasswordHash(
    val hash: String,
    val salt: String,
)
