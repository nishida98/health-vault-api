package com.healthvault.api.service

import com.github.f4b6a3.uuid.UuidCreator
import com.healthvault.api.dto.CreateUserAccountRequest
import com.healthvault.api.dto.UpdateUserAccountRequest
import com.healthvault.api.dto.UserAccountResponse
import com.healthvault.api.entity.UserAccount
import com.healthvault.api.exception.DuplicateEmailException
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.UserAccountNotFoundException
import com.healthvault.api.repository.UserAccountRepository
import com.healthvault.api.security.PasswordHasher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserAccountService(
    private val userAccountRepository: UserAccountRepository,
    private val passwordHasher: PasswordHasher,
) {
    @Transactional
    fun create(request: CreateUserAccountRequest): UserAccountResponse {
        ensureEmailIsAvailable(request.email)
        val password = passwordHasher.hash(request.password)

        return userAccountRepository.save(
            UserAccount(
                id = UuidCreator.getTimeOrderedEpoch(),
                name = request.name.trim(),
                nickname = request.nickname.trim(),
                email = request.email.normalizedEmail(),
                passwordHash = password.hash,
                passwordSalt = password.salt,
            ),
        ).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(): List<UserAccountResponse> {
        return userAccountRepository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): UserAccountResponse {
        return getUser(id).toResponse()
    }

    @Transactional
    fun replace(id: UUID, request: CreateUserAccountRequest): UserAccountResponse {
        val user = getUser(id)
        ensureEmailIsAvailable(request.email, id)
        val password = passwordHasher.hash(request.password)

        user.name = request.name.trim()
        user.nickname = request.nickname.trim()
        user.email = request.email.normalizedEmail()
        user.passwordHash = password.hash
        user.passwordSalt = password.salt

        return user.toResponse()
    }

    @Transactional
    fun update(id: UUID, request: UpdateUserAccountRequest): UserAccountResponse {
        val user = getUser(id)

        request.name?.let { user.name = it.requiredText("name") }
        request.nickname?.let { user.nickname = it.requiredText("nickname") }
        request.email?.let {
            val email = it.requiredText("email")
            ensureEmailIsAvailable(it, id)
            user.email = email.normalizedEmail()
        }
        request.password?.let {
            if (it.isBlank()) {
                throw InvalidUserInputException("password must not be blank.")
            }
            val password = passwordHasher.hash(it)
            user.passwordHash = password.hash
            user.passwordSalt = password.salt
        }

        return user.toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        if (!userAccountRepository.existsById(id)) {
            throw UserAccountNotFoundException()
        }

        userAccountRepository.deleteById(id)
    }

    private fun getUser(id: UUID): UserAccount {
        return userAccountRepository.findById(id)
            .orElseThrow { UserAccountNotFoundException() }
    }

    private fun ensureEmailIsAvailable(email: String, currentId: UUID? = null) {
        val normalizedEmail = email.normalizedEmail()
        val exists = currentId?.let {
            userAccountRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, it)
        } ?: userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)

        if (exists) {
            throw DuplicateEmailException()
        }
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

    private fun String.normalizedEmail(): String {
        return trim().lowercase()
    }

    private fun String.requiredText(fieldName: String): String {
        val value = trim()
        if (value.isBlank()) {
            throw InvalidUserInputException("$fieldName must not be blank.")
        }

        return value
    }
}
