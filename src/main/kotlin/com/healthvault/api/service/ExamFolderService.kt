package com.healthvault.api.service

import com.github.f4b6a3.uuid.UuidCreator
import com.healthvault.api.dto.CreateExamFolderRequest
import com.healthvault.api.dto.ExamFileResponse
import com.healthvault.api.dto.ExamFolderResponse
import com.healthvault.api.dto.ExamFolderTreeResponse
import com.healthvault.api.dto.MedicalExamResponse
import com.healthvault.api.entity.ExamFolder
import com.healthvault.api.entity.MedicalExam
import com.healthvault.api.exception.ExamFolderNotFoundException
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.UserAccountNotFoundException
import com.healthvault.api.observability.ApplicationMetrics
import com.healthvault.api.repository.ExamFolderRepository
import com.healthvault.api.repository.MedicalExamFileRepository
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ExamFolderService(
    private val examFolderRepository: ExamFolderRepository,
    private val medicalExamRepository: MedicalExamRepository,
    private val medicalExamFileRepository: MedicalExamFileRepository,
    private val userAccountRepository: UserAccountRepository,
    private val metrics: ApplicationMetrics,
) {
    @Transactional
    fun create(userId: UUID, request: CreateExamFolderRequest): ExamFolderResponse {
        val user = userAccountRepository.findById(userId)
            .orElseThrow { UserAccountNotFoundException() }
        val parent = request.parentId?.let { getFolder(userId, it) }
        val depth = (parent?.depth ?: 0) + 1

        if (depth > MAX_DEPTH) {
            throw InvalidUserInputException("Folders can have at most $MAX_DEPTH levels.")
        }

        val folder = examFolderRepository.save(
            ExamFolder(
                id = UuidCreator.getTimeOrderedEpoch(),
                user = user,
                parent = parent,
                name = request.name.requiredText("name"),
                depth = depth,
            ),
        )
        metrics.folderCreated()
        logger.info("exam_folder_created userId={} folderId={} parentId={} depth={}", userId, folder.id, parent?.id, depth)

        return folder.toResponse()
    }

    @Transactional(readOnly = true)
    fun findTree(userId: UUID): List<ExamFolderTreeResponse> {
        ensureUserExists(userId)
        val folders = examFolderRepository.findAllByUserIdOrderByDepthAscNameAsc(userId)
        val examsByFolderId = medicalExamRepository.findAllByUserIdOrderByPerformedAtDesc(userId)
            .groupBy { it.folder.id }

        return folders
            .filter { it.parent == null }
            .map { it.toTree(folders, examsByFolderId) }
    }

    private fun ExamFolder.toTree(
        folders: List<ExamFolder>,
        examsByFolderId: Map<UUID, List<MedicalExam>>,
    ): ExamFolderTreeResponse {
        return ExamFolderTreeResponse(
            id = id,
            name = name,
            depth = depth,
            parentId = parent?.id,
            exams = examsByFolderId[id].orEmpty().map { it.toExamResponse() },
            subfolders = folders
                .filter { it.parent?.id == id }
                .map { it.toTree(folders, examsByFolderId) },
        )
    }

    private fun ensureUserExists(userId: UUID) {
        if (!userAccountRepository.existsById(userId)) {
            throw UserAccountNotFoundException()
        }
    }

    private fun getFolder(userId: UUID, folderId: UUID): ExamFolder {
        ensureUserExists(userId)
        return examFolderRepository.findByIdAndUserId(folderId, userId)
            .orElseThrow { ExamFolderNotFoundException() }
    }

    private fun ExamFolder.toResponse(): ExamFolderResponse {
        return ExamFolderResponse(
            id = id,
            userId = user.id,
            parentId = parent?.id,
            name = name,
            depth = depth,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun MedicalExam.toExamResponse(): MedicalExamResponse {
        return MedicalExamResponse(
            id = id,
            userId = user.id,
            folderId = folder.id,
            performedAt = performedAt,
            requestingDoctor = requestingDoctor,
            examType = examType,
            result = result,
            files = medicalExamFileRepository.findAllByExamIdOrderByCreatedAtDesc(id).map {
                ExamFileResponse(
                    id = it.id,
                    examId = id,
                    fileName = it.fileName,
                    contentType = it.contentType,
                    sizeBytes = it.sizeBytes,
                    createdAt = it.createdAt,
                )
            },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun String.requiredText(fieldName: String): String {
        val value = trim()
        if (value.isBlank()) {
            throw InvalidUserInputException("$fieldName must not be blank.")
        }

        return value
    }

    private companion object {
        private const val MAX_DEPTH = 3
        private val logger = LoggerFactory.getLogger(ExamFolderService::class.java)
    }
}
