package com.healthvault.api.dto

import java.util.UUID

data class ExamFolderTreeResponse(
    val id: UUID,
    val name: String,
    val depth: Int,
    val parentId: UUID?,
    val exams: List<MedicalExamResponse>,
    val subfolders: List<ExamFolderTreeResponse>,
)
