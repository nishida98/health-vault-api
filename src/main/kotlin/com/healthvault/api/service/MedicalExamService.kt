package com.healthvault.api.service

import com.github.f4b6a3.uuid.UuidCreator
import com.healthvault.api.dto.CreateMedicalExamRequest
import com.healthvault.api.dto.MedicalExamResponse
import com.healthvault.api.dto.UpdateMedicalExamRequest
import com.healthvault.api.entity.MedicalExam
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.MedicalExamNotFoundException
import com.healthvault.api.exception.UserAccountNotFoundException
import com.healthvault.api.repository.MedicalExamRepository
import com.healthvault.api.repository.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MedicalExamService(
    private val medicalExamRepository: MedicalExamRepository,
    private val userAccountRepository: UserAccountRepository,
) {
    @Transactional
    fun create(userId: UUID, request: CreateMedicalExamRequest): MedicalExamResponse {
        val user = userAccountRepository.findById(userId)
            .orElseThrow { UserAccountNotFoundException() }

        return medicalExamRepository.save(
            MedicalExam(
                id = UuidCreator.getTimeOrderedEpoch(),
                user = user,
                performedAt = request.performedAt,
                requestingDoctor = request.requestingDoctor.requiredText("requestingDoctor"),
                result = request.result.requiredText("result"),
            ),
        ).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(userId: UUID): List<MedicalExamResponse> {
        ensureUserExists(userId)
        return medicalExamRepository.findAllByUserIdOrderByPerformedAtDesc(userId)
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
        exam.result = request.result.requiredText("result")

        return exam.toResponse()
    }

    @Transactional
    fun update(userId: UUID, examId: UUID, request: UpdateMedicalExamRequest): MedicalExamResponse {
        val exam = getExam(userId, examId)

        request.performedAt?.let { exam.performedAt = it }
        request.requestingDoctor?.let { exam.requestingDoctor = it.requiredText("requestingDoctor") }
        request.result?.let { exam.result = it.requiredText("result") }

        return exam.toResponse()
    }

    @Transactional
    fun delete(userId: UUID, examId: UUID) {
        val exam = getExam(userId, examId)
        medicalExamRepository.delete(exam)
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

    private fun MedicalExam.toResponse(): MedicalExamResponse {
        return MedicalExamResponse(
            id = id,
            userId = user.id,
            performedAt = performedAt,
            requestingDoctor = requestingDoctor,
            result = result,
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
}
