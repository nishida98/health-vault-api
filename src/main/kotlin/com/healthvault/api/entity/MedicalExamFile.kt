package com.healthvault.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "medical_exam_files")
open class MedicalExamFile(
    @Id
    @Column(nullable = false, updatable = false)
    open var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    open var exam: MedicalExam,

    @Column(nullable = false, name = "file_name", length = 255)
    open var fileName: String,

    @Column(nullable = false, name = "content_type", length = 120)
    open var contentType: String,

    @Column(nullable = false, name = "size_bytes")
    open var sizeBytes: Long,

    @Column(nullable = false, name = "object_key", length = 600, unique = true)
    open var objectKey: String,

    @Column(nullable = false, name = "created_at", updatable = false)
    open var createdAt: Instant = Instant.now(),
) {
    @PrePersist
    fun beforeInsert() {
        createdAt = Instant.now()
    }
}
