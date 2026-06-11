package com.healthvault.api.repository

import com.healthvault.api.entity.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<UserAccount>

    fun existsByEmailIgnoreCase(email: String): Boolean

    fun existsByEmailIgnoreCaseAndIdNot(email: String, id: UUID): Boolean
}
