package com.healthvault.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "exam_folders")
open class ExamFolder(
    @Id
    @Column(nullable = false, updatable = false)
    open var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: UserAccount,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    open var parent: ExamFolder?,

    @Column(nullable = false, length = 120)
    open var name: String,

    @Column(nullable = false)
    open var depth: Int,

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
