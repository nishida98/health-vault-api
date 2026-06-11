package com.healthvault.api.repository

import com.healthvault.api.entity.ExamFolder
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ExamFolderRepository : JpaRepository<ExamFolder, UUID> {
    fun findAllByUserIdOrderByDepthAscNameAsc(userId: UUID): List<ExamFolder>

    fun findByIdAndUserId(id: UUID, userId: UUID): Optional<ExamFolder>
}
