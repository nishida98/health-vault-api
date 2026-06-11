package com.healthvault.api.service

import com.healthvault.api.dto.LoginRequest
import com.healthvault.api.dto.LoginResponse
import com.healthvault.api.dto.TokenValidationRequest
import com.healthvault.api.dto.TokenValidationResponse
import com.healthvault.api.dto.UserAccountResponse
import com.healthvault.api.entity.UserAccount
import com.healthvault.api.exception.InvalidCredentialsException
import com.healthvault.api.repository.UserAccountRepository
import com.healthvault.api.security.JwtService
import com.healthvault.api.security.PasswordHasher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userAccountRepository: UserAccountRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) {
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val user = userAccountRepository.findByEmailIgnoreCase(request.email.trim())
            .orElseThrow { InvalidCredentialsException() }

        if (!passwordHasher.matches(request.password, user.passwordHash, user.passwordSalt)) {
            throw InvalidCredentialsException()
        }

        val token = jwtService.createToken(user)
        return LoginResponse(
            token = token.token,
            tokenType = "Bearer",
            expiresAt = token.expiresAt,
            user = user.toResponse(),
        )
    }

    fun validate(request: TokenValidationRequest): TokenValidationResponse {
        val token = jwtService.validate(request.token.trim())
        return TokenValidationResponse(
            valid = true,
            userId = token.userId,
            email = token.email,
            expiresAt = token.expiresAt,
        )
    }

    private fun UserAccount.toResponse(): UserAccountResponse {
        return UserAccountResponse(
            id = id,
            name = name,
            nickname = nickname,
            email = email,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
