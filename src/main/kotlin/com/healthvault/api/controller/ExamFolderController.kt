package com.healthvault.api.controller

import com.healthvault.api.dto.ApiResponse
import com.healthvault.api.dto.CreateExamFolderRequest
import com.healthvault.api.dto.ExamFolderResponse
import com.healthvault.api.dto.ExamFolderTreeResponse
import com.healthvault.api.service.ExamFolderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{userId}/exam-folders")
class ExamFolderController(
    private val examFolderService: ExamFolderService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: CreateExamFolderRequest,
    ): ApiResponse<ExamFolderResponse> {
        return ApiResponse(examFolderService.create(userId, request))
    }

    @GetMapping("/tree")
    fun findTree(@PathVariable userId: UUID): ApiResponse<List<ExamFolderTreeResponse>> {
        return ApiResponse(examFolderService.findTree(userId))
    }
}
