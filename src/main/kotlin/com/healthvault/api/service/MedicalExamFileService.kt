package com.healthvault.api.service

import com.github.f4b6a3.uuid.UuidCreator
import com.healthvault.api.dto.CreateExamFileUploadRequest
import com.healthvault.api.dto.ExamFileResponse
import com.healthvault.api.dto.PresignedFileUrlResponse
import com.healthvault.api.entity.MedicalExamFile
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.MedicalExamFileNotFoundException
import com.healthvault.api.exception.MedicalExamNotFoundException
import com.healthvault.api.exception.UserAccountNotFoundException
import com.healthvault.api.observability.ApplicationMetrics
import com.healthvault.api.repository.MedicalExamFileRepository
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import com.healthvault.api.storage.ObjectStorageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MedicalExamFileService(
    private val medicalExamRepository: MedicalExamRepository,
    private val medicalExamFileRepository: MedicalExamFileRepository,
    private val userAccountRepository: UserAccountRepository,
    private val objectStorageService: ObjectStorageService,
    private val metrics: ApplicationMetrics,
) {
    @Transactional
    fun createUploadUrl(
        userId: UUID,
        examId: UUID,
        request: CreateExamFileUploadRequest,
    ): PresignedFileUrlResponse {
        val exam = getExam(userId, examId)
        val fileId = UuidCreator.getTimeOrderedEpoch()
        val fileName = request.fileName.requiredText("fileName")
        val contentType = request.contentType.requiredText("contentType")
        val objectKey = "users/$userId/exams/$examId/files/$fileId/${fileName.safeObjectName()}"

        val file = medicalExamFileRepository.save(
            MedicalExamFile(
                id = fileId,
                exam = exam,
                fileName = fileName,
                contentType = contentType,
                sizeBytes = request.sizeBytes,
                objectKey = objectKey,
            ),
        )
        val presignedUrl = objectStorageService.createUploadUrl(objectKey, contentType)
        metrics.examFileUploadUrlCreated()
        logger.info("exam_file_upload_url_created userId={} examId={} fileId={}", userId, examId, fileId)

        return PresignedFileUrlResponse(
            file = file.toResponse(),
            method = presignedUrl.method,
            url = presignedUrl.url,
            headers = presignedUrl.headers,
            expiresAt = presignedUrl.expiresAt,
        )
    }

    @Transactional(readOnly = true)
    fun findAll(userId: UUID, examId: UUID): List<ExamFileResponse> {
        ensureExamExists(userId, examId)
        return medicalExamFileRepository.findAllByExamIdOrderByCreatedAtDesc(examId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun createDownloadUrl(userId: UUID, examId: UUID, fileId: UUID): PresignedFileUrlResponse {
        val file = getFile(userId, examId, fileId)
        val presignedUrl = objectStorageService.createDownloadUrl(file.objectKey)
        metrics.examFileDownloadUrlCreated()
        logger.info("exam_file_download_url_created userId={} examId={} fileId={}", userId, examId, fileId)

        return PresignedFileUrlResponse(
            file = file.toResponse(),
            method = presignedUrl.method,
            url = presignedUrl.url,
            headers = presignedUrl.headers,
            expiresAt = presignedUrl.expiresAt,
        )
    }

    @Transactional
    fun delete(userId: UUID, examId: UUID, fileId: UUID) {
        val file = getFile(userId, examId, fileId)
        medicalExamFileRepository.delete(file)
        metrics.examFileDeleted()
        logger.info("exam_file_deleted userId={} examId={} fileId={}", userId, examId, fileId)
    }

    private fun getExam(userId: UUID, examId: UUID) =
        medicalExamRepository.findByIdAndUserId(examId, userId)
            .orElseThrow {
                if (userAccountRepository.existsById(userId)) MedicalExamNotFoundException() else UserAccountNotFoundException()
            }

    private fun ensureExamExists(userId: UUID, examId: UUID) {
        getExam(userId, examId)
    }

    private fun getFile(userId: UUID, examId: UUID, fileId: UUID): MedicalExamFile {
        ensureExamExists(userId, examId)
        return medicalExamFileRepository.findByIdAndExamId(fileId, examId)
            .orElseThrow { MedicalExamFileNotFoundException() }
    }

    private fun MedicalExamFile.toResponse(): ExamFileResponse {
        return ExamFileResponse(
            id = id,
            examId = exam.id,
            fileName = fileName,
            contentType = contentType,
            sizeBytes = sizeBytes,
            createdAt = createdAt,
        )
    }

    private fun String.requiredText(fieldName: String): String {
        val value = trim()
        if (value.isBlank()) {
            throw InvalidUserInputException("$fieldName must not be blank.")
        }
        return value
    }

    private fun String.safeObjectName(): String {
        return trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(MAX_OBJECT_NAME_LENGTH)
            .ifBlank { "file" }
    }

    private companion object {
        private const val MAX_OBJECT_NAME_LENGTH = 120
        private val logger = LoggerFactory.getLogger(MedicalExamFileService::class.java)
    }
}
