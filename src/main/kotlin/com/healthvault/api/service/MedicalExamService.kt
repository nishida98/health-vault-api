package com.healthvault.api.service

import com.github.f4b6a3.uuid.UuidCreator
import com.healthvault.api.dto.CreateMedicalExamRequest
import com.healthvault.api.dto.ExamFileResponse
import com.healthvault.api.dto.MedicalExamResponse
import com.healthvault.api.dto.MoveMedicalExamRequest
import com.healthvault.api.dto.UpdateMedicalExamRequest
import com.healthvault.api.entity.MedicalExam
import com.healthvault.api.exception.ExamFolderNotFoundException
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.MedicalExamNotFoundException
import com.healthvault.api.exception.UserAccountNotFoundException
import com.healthvault.api.observability.ApplicationMetrics
import com.healthvault.api.repository.ExamFolderRepository
import com.healthvault.api.repository.MedicalExamFileRepository
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class MedicalExamService(
    private val medicalExamRepository: MedicalExamRepository,
    private val medicalExamFileRepository: MedicalExamFileRepository,
    private val examFolderRepository: ExamFolderRepository,
    private val userAccountRepository: UserAccountRepository,
    private val metrics: ApplicationMetrics,
) {
    @Transactional
    fun create(userId: UUID, request: CreateMedicalExamRequest): MedicalExamResponse {
        val user = userAccountRepository.findById(userId)
            .orElseThrow { UserAccountNotFoundException() }
        val folder = getFolder(userId, request.folderId)

        val exam = medicalExamRepository.save(
            MedicalExam(
                id = UuidCreator.getTimeOrderedEpoch(),
                user = user,
                performedAt = request.performedAt,
                requestingDoctor = request.requestingDoctor.requiredText("requestingDoctor"),
                examType = request.examType.requiredText("examType"),
                result = request.result.requiredText("result"),
                folder = folder,
            ),
        )
        metrics.examCreated()
        logger.info("medical_exam_created userId={} examId={} folderId={}", userId, exam.id, folder.id)

        return exam.toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(userId: UUID, date: String?, doctor: String?, examType: String?): List<MedicalExamResponse> {
        ensureUserExists(userId)
        return medicalExamRepository.findAllByUserIdOrderByPerformedAtDesc(userId)
            .filter { it.matches(date = date, doctor = doctor, examType = examType) }
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(userId: UUID, examId: UUID): MedicalExamResponse {
        return getExam(userId, examId).toResponse()
    }

    @Transactional
    fun replace(userId: UUID, examId: UUID, request: CreateMedicalExamRequest): MedicalExamResponse {
        val exam = getExam(userId, examId)
        exam.performedAt = request.performedAt
        exam.requestingDoctor = request.requestingDoctor.requiredText("requestingDoctor")
        exam.examType = request.examType.requiredText("examType")
        exam.result = request.result.requiredText("result")
        exam.folder = getFolder(userId, request.folderId)

        return exam.toResponse()
    }

    @Transactional
    fun update(userId: UUID, examId: UUID, request: UpdateMedicalExamRequest): MedicalExamResponse {
        val exam = getExam(userId, examId)

        request.performedAt?.let { exam.performedAt = it }
        request.requestingDoctor?.let { exam.requestingDoctor = it.requiredText("requestingDoctor") }
        request.examType?.let { exam.examType = it.requiredText("examType") }
        request.result?.let { exam.result = it.requiredText("result") }
        request.folderId?.let { exam.folder = getFolder(userId, it) }

        return exam.toResponse()
    }

    @Transactional
    fun move(userId: UUID, examId: UUID, request: MoveMedicalExamRequest): MedicalExamResponse {
        val exam = getExam(userId, examId)
        exam.folder = getFolder(userId, request.folderId)
        metrics.examMoved()
        logger.info("medical_exam_moved userId={} examId={} folderId={}", userId, examId, request.folderId)

        return exam.toResponse()
    }

    @Transactional
    fun delete(userId: UUID, examId: UUID) {
        val exam = getExam(userId, examId)
        medicalExamFileRepository.deleteAllByExamId(examId)
        medicalExamRepository.delete(exam)
        metrics.examDeleted()
        logger.info("medical_exam_deleted userId={} examId={}", userId, examId)
    }

    private fun ensureUserExists(userId: UUID) {
        if (!userAccountRepository.existsById(userId)) {
            throw UserAccountNotFoundException()
        }
    }

    private fun getExam(userId: UUID, examId: UUID): MedicalExam {
        ensureUserExists(userId)
        return medicalExamRepository.findByIdAndUserId(examId, userId)
            .orElseThrow { MedicalExamNotFoundException() }
    }

    private fun getFolder(userId: UUID, folderId: UUID) =
        examFolderRepository.findByIdAndUserId(folderId, userId)
            .orElseThrow { ExamFolderNotFoundException() }

    private fun MedicalExam.toResponse(): MedicalExamResponse {
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

    private fun MedicalExam.matches(date: String?, doctor: String?, examType: String?): Boolean {
        return performedAt.format(DateTimeFormatter.ISO_DATE).contains(date.orEmpty(), ignoreCase = true) &&
            requestingDoctor.contains(doctor.orEmpty(), ignoreCase = true) &&
            this.examType.contains(examType.orEmpty(), ignoreCase = true)
    }

    private fun String.requiredText(fieldName: String): String {
        val value = trim()
        if (value.isBlank()) {
            throw InvalidUserInputException("$fieldName must not be blank.")
        }

        return value
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(MedicalExamService::class.java)
    }
}
