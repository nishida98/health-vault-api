package com.healthvault.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateExamFolderRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    val parentId: UUID?,
)
