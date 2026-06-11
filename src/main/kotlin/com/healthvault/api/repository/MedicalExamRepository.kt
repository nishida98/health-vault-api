package com.healthvault.api.repository

import com.healthvault.api.entity.MedicalExam
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface MedicalExamRepository : JpaRepository<MedicalExam, UUID> {
    fun findAllByUserIdOrderByPerformedAtDesc(userId: UUID): List<MedicalExam>

    fun findByIdAndUserId(id: UUID, userId: UUID): Optional<MedicalExam>

    fun findAllByUserIdAndFolderIdOrderByPerformedAtDesc(userId: UUID, folderId: UUID): List<MedicalExam>
}
