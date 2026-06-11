package com.healthvault.api.controller

import com.healthvault.api.dto.ApiResponse
import com.healthvault.api.dto.LoginRequest
import com.healthvault.api.dto.LoginResponse
import com.healthvault.api.dto.TokenValidationRequest
import com.healthvault.api.dto.TokenValidationResponse
import com.healthvault.api.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<LoginResponse> {
        return ApiResponse(authService.login(request))
    }

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody request: TokenValidationRequest): ApiResponse<TokenValidationResponse> {
        return ApiResponse(authService.validate(request))
    }
}
