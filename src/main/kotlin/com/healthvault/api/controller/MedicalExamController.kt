package com.healthvault.api.controller

import com.healthvault.api.dto.ApiResponse
import com.healthvault.api.dto.CreateMedicalExamRequest
import com.healthvault.api.dto.MedicalExamResponse
import com.healthvault.api.dto.MoveMedicalExamRequest
import com.healthvault.api.dto.UpdateMedicalExamRequest
import com.healthvault.api.service.MedicalExamService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{userId}/exams")
class MedicalExamController(
    private val medicalExamService: MedicalExamService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: CreateMedicalExamRequest,
    ): ApiResponse<MedicalExamResponse> {
        return ApiResponse(medicalExamService.create(userId, request))
    }

    @GetMapping
    fun findAll(
        @PathVariable userId: UUID,
        @RequestParam(required = false) date: String?,
        @RequestParam(required = false) doctor: String?,
        @RequestParam(required = false) examType: String?,
    ): ApiResponse<List<MedicalExamResponse>> {
        return ApiResponse(medicalExamService.findAll(userId, date, doctor, examType))
    }

    @GetMapping("/{examId}")
    fun findById(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
    ): ApiResponse<MedicalExamResponse> {
        return ApiResponse(medicalExamService.findById(userId, examId))
    }

    @PutMapping("/{examId}")
    fun replace(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @Valid @RequestBody request: CreateMedicalExamRequest,
    ): ApiResponse<MedicalExamResponse> {
        return ApiResponse(medicalExamService.replace(userId, examId, request))
    }

    @PatchMapping("/{examId}")
    fun update(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @Valid @RequestBody request: UpdateMedicalExamRequest,
    ): ApiResponse<MedicalExamResponse> {
        return ApiResponse(medicalExamService.update(userId, examId, request))
    }

    @PatchMapping("/{examId}/folder")
    fun move(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
        @Valid @RequestBody request: MoveMedicalExamRequest,
    ): ApiResponse<MedicalExamResponse> {
        return ApiResponse(medicalExamService.move(userId, examId, request))
    }

    @DeleteMapping("/{examId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable userId: UUID,
        @PathVariable examId: UUID,
    ) {
        medicalExamService.delete(userId, examId)
    }
}
