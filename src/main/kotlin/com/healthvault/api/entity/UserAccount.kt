package com.healthvault.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_accounts")
open class UserAccount(
    @Id
    @Column(nullable = false, updatable = false)
    open var id: UUID,

    @Column(nullable = false, length = 120)
    open var name: String,

    @Column(nullable = false, length = 80)
    open var nickname: String,

    @Column(nullable = false, unique = true, length = 180)
    open var email: String,

    @Column(nullable = false, name = "password_hash", length = 344)
    open var passwordHash: String,

    @Column(nullable = false, name = "password_salt", length = 44)
    open var passwordSalt: String,

    @Column(nullable = false, name = "created_at", updatable = false)
    open var createdAt: Instant = Instant.now(),

    @Column(nullable = false, name = "updated_at")
    open var updatedAt: Instant = Instant.now(),
) {
    @PrePersist
    fun beforeInsert() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun beforeUpdate() {
        updatedAt = Instant.now()
    }
}
