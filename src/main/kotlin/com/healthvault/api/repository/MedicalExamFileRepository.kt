package com.healthvault.api.repository

import com.healthvault.api.entity.MedicalExamFile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface MedicalExamFileRepository : JpaRepository<MedicalExamFile, UUID> {
    fun findAllByExamIdOrderByCreatedAtDesc(examId: UUID): List<MedicalExamFile>

    fun findByIdAndExamId(id: UUID, examId: UUID): Optional<MedicalExamFile>

    fun deleteAllByExamId(examId: UUID)
}
