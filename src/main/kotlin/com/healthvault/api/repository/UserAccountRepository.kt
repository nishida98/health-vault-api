package com.healthvault.api.repository

import com.healthvault.api.entity.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
    fun existsByEmailIgnoreCase(email: String): Boolean

    fun existsByEmailIgnoreCaseAndIdNot(email: String, id: UUID): Boolean
}
