package com.healthvault.api.controller

import com.healthvault.api.dto.ApiResponse
import com.healthvault.api.dto.CreateUserAccountRequest
import com.healthvault.api.dto.UpdateUserAccountRequest
import com.healthvault.api.dto.UserAccountResponse
import com.healthvault.api.service.UserAccountService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserAccountController(
    private val userAccountService: UserAccountService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserAccountRequest): ApiResponse<UserAccountResponse> {
        return ApiResponse(userAccountService.create(request))
    }

    @GetMapping
    fun findAll(): ApiResponse<List<UserAccountResponse>> {
        return ApiResponse(userAccountService.findAll())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ApiResponse<UserAccountResponse> {
        return ApiResponse(userAccountService.findById(id))
    }

    @PutMapping("/{id}")
    fun replace(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateUserAccountRequest,
    ): ApiResponse<UserAccountResponse> {
        return ApiResponse(userAccountService.replace(id, request))
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserAccountRequest,
    ): ApiResponse<UserAccountResponse> {
        return ApiResponse(userAccountService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        userAccountService.delete(id)
    }
}
