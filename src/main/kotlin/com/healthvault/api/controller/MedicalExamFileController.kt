package com.healthvault.api.controller

import com.healthvault.api.dto.ApiResponse
import com.healthvault.api.dto.CreateExamFileUploadRequest
import com.healthvault.api.dto.ExamFileResponse
import com.healthvault.api.dto.PresignedFileUrlResponse
import com.healthvault.api.service.MedicalExamFileService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{userId}/exams/{examId}/files")
class MedicalExamFileController(
    private val medicalExamFileService: MedicalExamFileService,
) {
    @PostMapping("/upload-url")
    @ResponseStatus(HttpStatus.CREATED)
    fun createUploadUrl(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @Valid @RequestBody request: CreateExamFileUploadRequest,
    ): ApiResponse<PresignedFileUrlResponse> {
        return ApiResponse(medicalExamFileService.createUploadUrl(userId, examId, request))
    }

    @GetMapping
    fun findAll(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
    ): ApiResponse<List<ExamFileResponse>> {
        return ApiResponse(medicalExamFileService.findAll(userId, examId))
    }

    @GetMapping("/{fileId}/download-url")
    fun createDownloadUrl(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @PathVariable fileId: UUID,
    ): ApiResponse<PresignedFileUrlResponse> {
        return ApiResponse(medicalExamFileService.createDownloadUrl(userId, examId, fileId))
    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @PathVariable fileId: UUID,
    ) {
        medicalExamFileService.delete(userId, examId, fileId)
    }
}
