package com.healthvault.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UpdateUserAccountRequest(
    @field:Size(max = 120)
    val name: String?,

    @field:Size(max = 80)
    val nickname: String?,

    @field:Email
    @field:Size(max = 180)
    val email: String?,

    @field:Size(min = 8, max = 120)
    val password: String?,
)
